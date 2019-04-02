package es.elixir.bsc.openebench.biotools.git;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

/**
 * @author Dmitry Repchevsky
 */

public class JgitSytemReaderHack extends SystemReader {

    private final SystemReader sr;

    public JgitSytemReaderHack(SystemReader sr) {
        this.sr = sr;
    }

    @Override
    public void checkPath(String path) throws CorruptObjectException {
    }

    @Override
    public void checkPath(byte[] path) throws CorruptObjectException {
    }


    @Override
    public String getHostname() {
        return sr.getHostname();
    }

    @Override
    public String getenv(String arg0) {
        return sr.getenv(arg0);
    }

    @Override
    public String getProperty(String arg0) {
        return sr.getProperty(arg0);
    }

    @Override
    public FileBasedConfig openUserConfig(Config arg0, FS arg1) {
        return sr.openUserConfig(arg0, arg1);
    }

    @Override
    public FileBasedConfig openSystemConfig(Config arg0, FS arg1) {
        return sr.openSystemConfig(arg0, arg1);
    }

    @Override
    public long getCurrentTime() {
        return sr.getCurrentTime();
    }

    @Override
    public int getTimezone(long arg0) {
        return sr.getTimezone(arg0);
    }
}
