/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.solr.handler.admin.directory;

import com.qlangtech.tis.fs.FSDataInputStream;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.IPathInfo;
import com.qlangtech.tis.fs.ITISFileSystem;
import org.apache.lucene.store.*;
import org.apache.solr.store.blockcache.CustomBufferedIndexInput;
import org.apache.solr.store.hdfs.HdfsDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISDirectory extends BaseDirectory {

    public static Logger LOG = LoggerFactory.getLogger(HdfsDirectory.class);

    public static final int BUFFER_SIZE = 8192;

    private static final String LF_EXT = ".lf";

    protected final IPath fsDirPath;


    private final ITISFileSystem fileSystem;

    private final Set<String> readFiles = new HashSet<>();

    public TISDirectory(IPath hdfsDirPath, ITISFileSystem fileSystem) throws IOException {
        this(hdfsDirPath, NoLockFactory.INSTANCE, fileSystem);
    }

    @Override
    public ChecksumIndexInput openChecksumInput(String name, IOContext context) throws IOException {
        return super.openChecksumInput(name, context);
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    private TISDirectory(IPath hdfsDirPath, LockFactory lockFactory, ITISFileSystem fileSystem) throws IOException {
        super(lockFactory);
        this.fsDirPath = hdfsDirPath;
        this.fileSystem = fileSystem;
    }

    @Override
    public void close() throws IOException {
        LOG.info("Closing hdfs directory {}", fsDirPath);
        fileSystem.close();
        isOpen = false;
    }

    @Override
    public Set<String> getPendingDeletions() throws IOException {
        return Collections.emptySet();
    }

    /**
     * Check whether this directory is open or closed. This check may return
     * stale results in the form of false negatives.
     *
     * @return true if the directory is definitely closed, false if the
     * directory is open or is pending closure
     */
    public boolean isClosed() {
        return !isOpen;
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        return new TisFsFileWriter(getFileSystem(), getChildPath(name), name);
    }

    private String[] getNormalNames(List<String> files) {
        int size = files.size();
        for (int i = 0; i < size; i++) {
            String str = files.get(i);
            files.set(i, toNormalName(str));
        }
        return files.toArray(new String[]{});
    }

    private String toNormalName(String name) {
        if (name.endsWith(LF_EXT)) {
            return name.substring(0, name.length() - 3);
        }
        return name;
    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        return openInput(name, BUFFER_SIZE);
    }

    private IndexInput openInput(String name, int bufferSize) throws IOException {
        return new TISFSIndexInput(name, getFileSystem(), getChildPath(name), bufferSize);
    }

    @Override
    public void deleteFile(String name) throws IOException {
        IPath path = getChildPath(name);
        LOG.debug("Deleting {}", path);
        getFileSystem().delete(path, false);
    }

    @Override
    public void syncMetaData() throws IOException {
    }

    @Override
    public void rename(String source, String dest) throws IOException {
        // Path sourcePath = new Path(hdfsDirPath, source);
        // Path destPath = new Path(hdfsDirPath, dest);
        // fileContext.rename(sourcePath, destPath);
    }

    // @Override
    // public void renameFile(String source, String dest) throws IOException {
    // // Path sourcePath = new Path(hdfsDirPath, source);
    // // Path destPath = new Path(hdfsDirPath, dest);
    // // fileContext.rename(sourcePath, destPath);
    // }
    @Override
    public long fileLength(String name) throws IOException {
        IPath path = getChildPath(name);
        return getLength(getFileSystem(), path);
    }

    public long fileModified(String name) throws IOException {
        IPath path = getChildPath(name);
        IPathInfo fileStatus = fileSystem.getFileInfo(path);
        return fileStatus.getModificationTime();
    }

    private IPath getChildPath(String name) {
        return this.fileSystem.getPath(fsDirPath, name);
    }

    public static long getLength(ITISFileSystem fileSystem, IPath path) throws IOException {
        IPathInfo fileStatus = fileSystem.getFileInfo(path);
        return fileStatus.getLength();
    }

    @Override
    public String[] listAll() throws IOException {
        List<IPathInfo> listStatus = getFileSystem().listChildren(fsDirPath);
        // FileStatus[] listStatus = getFileSystem().listStatus(fsDirPath);
        List<String> files = new ArrayList<>();
        if (listStatus == null) {
            return new String[]{};
        }
        for (IPathInfo status : listStatus) {
            files.add(status.getPath().getName());
        }
        return getNormalNames(files);
    }

    public IPath getFsDirPath() {
        return fsDirPath;
    }

    public ITISFileSystem getFileSystem() {
        return fileSystem;
    }

    public static Logger logger = LoggerFactory.getLogger(TISFSIndexInput.class);

    private static class TISFSIndexInput extends CustomBufferedIndexInput {

        private final IPath path;

        private final FSDataInputStream inputStream;

        private final long length;

        private boolean clone = false;

        public TISFSIndexInput(String name, ITISFileSystem fileSystem, IPath path, int bufferSize) throws IOException {
            super(name);
            this.path = path;
            length = fileSystem.getFileInfo(path).getLength();
            inputStream = fileSystem.open(path, bufferSize);
        }

        @Override
        protected void readInternal(byte[] b, int offset, int readLength) throws IOException {
            inputStream.readFully(getFilePointer(), b, offset, readLength);
        }

        @Override
        protected void seekInternal(long pos) throws IOException {
            inputStream.seek(pos);
        }

        @Override
        protected void closeInternal() throws IOException {
            logger.debug("Closing normal index input on {}", path);
            if (!clone) {
                inputStream.close();
            }
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public IndexInput clone() {
            TISFSIndexInput clone = (TISFSIndexInput) super.clone();
            clone.clone = true;
            return clone;
        }
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        LOG.debug("Sync called on {}", Arrays.toString(names.toArray()));
    }

    @Override
    public int hashCode() {
        return fsDirPath.hashCode();
    }

    public static class TisFsFileWriter extends OutputStreamIndexOutput {

        public static final String HDFS_SYNC_BLOCK = "solr.hdfs.sync.block";

        public static final int BUFFER_SIZE = 16384;

        public TisFsFileWriter(ITISFileSystem fileSystem, IPath path, String name) throws IOException {
            super("fileSystem=" + fileSystem.getName() + " path=" + path, name, getOutputStream(fileSystem, path), 16384);
        }

        private static final OutputStream getOutputStream(ITISFileSystem fileSystem, IPath path) throws IOException {
            // }
            return fileSystem.create(path, true);
        }
    }

}
