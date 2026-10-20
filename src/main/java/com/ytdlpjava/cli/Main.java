package com.ytdlpjava.cli;

import com.beust.jcommander.JCommander;
import com.ytdlpjava.config.AppConfig;
import com.ytdlpjava.config.ApplicationFactory;
import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.core.YtdlManager;
import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.VideoTask;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) {
        ShutdownCancellation cancellation = new ShutdownCancellation();
        int status;
        try (cancellation) {
            status = run(args);
        }
        if (status != 0 && !cancellation.isShuttingDown()) System.exit(status);
    }

    public static int run(String[] args) {
        return run(args, new ApplicationFactory());
    }

    // The CLI owns the factory for the entire batch, including failure paths.
    public static int run(String[] args, ApplicationFactory factory) {
        try (factory) {
            AppConfig config = new AppConfig();
            JCommander jc = JCommander.newBuilder().addObject(config).build();
            try {
                jc.parse(args);
                if (config.isHelp()) {
                    jc.usage();
                    return 0;
                }
                config.validate();
                if (config.getVideoUrls().isEmpty()) {
                    new InteractivePromptService().runInteractive(config);
                }
                config.validate();
            } catch (IllegalArgumentException | com.beust.jcommander.ParameterException e) {
                System.err.println(e.getMessage());
                jc.usage();
                return 2;
            }
            config.configureOutputDir();
            ProcessExecutor executor = factory.createProcessExecutor();
            Downloader downloader = factory.createDownloader(config, executor);
            VideoTask task = factory.createTask(config, downloader, factory.createFilenameProvider(), executor);
            YtdlManager manager = new YtdlManager(task, downloader);
            List<String> urls = config.getVideoUrls().isEmpty()
                    ? List.of(config.getVideoUrl()) : config.getVideoUrls();
            int failures = 0;
            for (String url : urls) {
                try {
                    manager.process(url, Path.of(config.getOutputDir()));
                } catch (Exception e) {
                    failures++;
                    log.error("Processing failed for {}: {}", url, e.getMessage());
                    if (Thread.currentThread().isInterrupted()) break;
                }
            }
            return failures == 0 ? 0 : 1;
        } catch (Exception e) {
            log.error("Application failed: {}", e.getMessage());
            return 1;
        }
    }
}
