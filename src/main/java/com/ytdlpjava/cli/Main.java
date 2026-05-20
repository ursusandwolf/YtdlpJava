package com.ytdlpjava.cli;

import com.beust.jcommander.JCommander;
import com.ytdlpjava.config.AppConfig;
import com.ytdlpjava.config.ApplicationFactory;
import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.core.YtdlManager;
import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.FilenameProvider;
import com.ytdlpjava.model.VideoTask;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;

@Slf4j
public class Main {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        JCommander jc = JCommander.newBuilder().addObject(config).build();
        try {
            jc.parse(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        if (config.isHelp()) {
            jc.usage();
            return;
        }

        if (config.getVideoUrls() == null || config.getVideoUrls().isEmpty()) {
            new InteractivePromptService().runInteractive(config);
        }

        config.configureOutputDir();

        ApplicationFactory factory = new ApplicationFactory();
        ProcessExecutor executor = factory.createProcessExecutor();
        FilenameProvider filenameProvider = factory.createFilenameProvider();
        
        Downloader downloader = factory.createDownloader(config, executor);
        VideoTask task = factory.createTask(config, downloader, filenameProvider, executor);

        YtdlManager manager = new YtdlManager(task, downloader);
        try {
            if (config.getVideoUrls() != null && !config.getVideoUrls().isEmpty()) {
                for (String url : config.getVideoUrls()) {
                    manager.process(url, Path.of(config.getOutputDir()));
                }
            } else if (config.getVideoUrl() != null) {
                manager.process(config.getVideoUrl(), Path.of(config.getOutputDir()));
            }
        } catch (Exception e) {
            log.error("Process failed: {}", e.getMessage());
            System.exit(1);
        }
    }
}
