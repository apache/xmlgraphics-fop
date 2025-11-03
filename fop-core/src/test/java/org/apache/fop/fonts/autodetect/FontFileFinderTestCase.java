/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fonts.autodetect;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.fonts.FontEventListener;

public class FontFileFinderTestCase {

    @Test
    public void testSymlinkRecursion() throws IOException {
        FontFileFinder finder = new FontFileFinder(null);

        File targetDirectory = createMockDirectory("target");
        File linkDirectory = createMockDirectory("link");

        setupChildFiles(targetDirectory, linkDirectory);
        setupChildFiles(linkDirectory, targetDirectory);

        setupSymLink(linkDirectory.toPath());

        finder.find(targetDirectory);

        //targetDir -> linkDir -> targetDir -> linkDir (skipped as it has been seen)
        //it's repeating the folder because we need to access the symlink once only.
        //in this scenario, the symlink is recursive, hence the repetition
        verify(targetDirectory, times(2)).listFiles(any(FileFilter.class));
        verify(linkDirectory, times(1)).listFiles(any(FileFilter.class));

        //this is only accessed for symlinks
        // it's used to make sure we don't access the same synlink twice
        verify(targetDirectory.toPath(), times(0)).toRealPath();
        verify(linkDirectory.toPath(), times(2)).toRealPath();
    }

    @Test
    public void testNullEventListener() throws IOException {
        FontFileFinder finder = new FontFileFinder(null);
        try {
            finder.find(new File(""));
        } catch (NullPointerException e) {
            fail("Should not throw NullPointerException when event listener is null");
        }
    }

    @Test
    public void testValidEventListener() throws IOException {
        FontEventListener mockListener = mock(FontEventListener.class);
        FontFileFinder finder = new FontFileFinder(mockListener);

        finder.find(new File("fop"));

        verify(mockListener, times(1)).fontDirectoryNotFound(any(), any());
    }

    private File createMockDirectory(String path) throws IOException {
        Path mockRealPath = mock(Path.class);
        when(mockRealPath.toString()).thenReturn(path);

        Path mockPath = mock(Path.class);
        when(mockPath.toRealPath()).thenReturn(mockRealPath);
        setupPathFileSystem(mockPath, false);

        File mockDir = mock(File.class);
        when(mockDir.isDirectory()).thenReturn(true);
        when(mockDir.toPath()).thenReturn(mockPath);
        when(mockDir.getName()).thenReturn(path);

        return mockDir;
    }

    private void setupChildFiles(File dir, File childFile) {
        when(dir.listFiles(any(FileFilter.class))).thenReturn(new File[]{childFile});
    }

    private void setupSymLink(Path linkPath) throws IOException {
        setupPathFileSystem(linkPath, true);
    }

    private void setupPathFileSystem(Path path, boolean symlink) throws IOException {
        BasicFileAttributes mockBasicFileAttributes = mock(BasicFileAttributes.class);
        when(mockBasicFileAttributes.isSymbolicLink()).thenReturn(symlink);

        FileSystemProvider mockProvider = mock(FileSystemProvider.class);
        when(mockProvider.readAttributes(any(), any(Class.class), any())).thenReturn(mockBasicFileAttributes);

        FileSystem mockFileSystem = mock(FileSystem.class);
        when(mockFileSystem.provider()).thenReturn(mockProvider);
        when(path.getFileSystem()).thenReturn(mockFileSystem);
    }
}
