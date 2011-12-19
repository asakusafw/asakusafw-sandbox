/**
 * Copyright 2011 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.windgate.stream.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.stream.StreamProvider;

/**
 * An implementation of {@link StreamProvider} for output.
 * @since 0.2.2
 */
public class FileOutputStreamProvider implements StreamProvider<OutputStream> {

    static final Logger LOG = LoggerFactory.getLogger(FileOutputStreamProvider.class);

    private static final int BUFFER_SIZE = 1024 * 64;

    private final File file;

    /**
     * Creates a new instance.
     * @param file target file to create
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileOutputStreamProvider(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null"); //$NON-NLS-1$
        }
        this.file = file;
    }

    @Override
    public String getDescription() {
        return MessageFormat.format(
                "File({0})",
                file.getAbsolutePath());
    }

    @Override
    public OutputStream open() throws IOException {
        LOG.debug("Opening file to write: {}",
                file);
        boolean succeed = false;
        File parent = file.getParentFile();
        for (int i = 0; i < 10; i++) {
            if (parent == null || parent.exists()) {
                break;
            }
            LOG.debug("Parent directory does not exist, will be created: {}",
                    file);
            if (parent.mkdirs()) {
                break;
            }
        }
        if (parent != null && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create parent directory: {0}",
                    parent.getAbsolutePath()));
        }
        FileOutputStream stream = new FileOutputStream(file);
        try {
            OutputStream result = new BufferedOutputStream(stream, BUFFER_SIZE);
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }
}
