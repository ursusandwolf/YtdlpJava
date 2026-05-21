package com.ytdlpjava.subtitle.api;

import com.ytdlpjava.subtitle.model.SubtitleBlock;
import java.util.List;

public interface SubtitleProcessor {
    List<String> process(List<SubtitleBlock> blocks);
}
