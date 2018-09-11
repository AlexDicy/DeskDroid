package us.halex.deskdroid.execute;

import android.util.Log;

import org.x.android.XServerNative;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import us.halex.deskdroid.DeskDroidApp;

/**
 * Created by HAlexTM on 10/09/2018 18:36
 */
public abstract class Executor {
    private Map<String, String> env = new HashMap<>();
    private String executable;
    private String[] arguments;
    private boolean waitFor = false;
    private boolean runShell = false;

    private Process process;

    private Executor() {
        arguments = new String[0];
    }

    public Executor(String executable, String arguments) {
        this(executable, arguments.split(" "));
    }

    public Executor(String executable, String[] arguments) {
        this.executable = executable;
        this.arguments = arguments;
    }


    public void addEnv(String key, String value) {
        env.put(key, value);
    }

    public void removeEnv(String key) {
        env.remove(key);
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void setRunShell(boolean runShell) {
        this.runShell = runShell;
    }

    public void setWaitFor(boolean waitFor) {
        this.waitFor = waitFor;
    }

    public void execute() {
        run();

        File bin = new File(DeskDroidApp.getAppFolder(), "bin");
        env.put("DISPLAY", ":0.0");
        if (XServerNative.getenv("PATH") != null) {
            env.put("PATH", XServerNative.getenv("PATH") + ":" + bin.getAbsolutePath());
        } else {
            env.put("PATH", bin.getAbsolutePath());
        }

        String libsPath = new File(DeskDroidApp.getAppFolder(), "lib").getAbsolutePath();
        if (XServerNative.getenv("LD_LIBRARY_PATH") != null) {
            libsPath += ":" + new File(XServerNative.getenv("LD_LIBRARY_PATH")).getAbsolutePath();
        }
        env.put("LD_LIBRARY_PATH", libsPath);
        String cacheFolder = DeskDroidApp.getCacheFolder().getAbsolutePath();
        env.put("HOME", DeskDroidApp.getHomeFolder().getAbsolutePath());
        env.put("TMP", cacheFolder);
        env.put("TEMP", cacheFolder);
        env.put("TMPDIR", cacheFolder);
        env.put("LANG", DeskDroidApp.getLanguage() + "_" + DeskDroidApp.getCountry() + ".UTF-8");
        env.put("LOCALE", DeskDroidApp.getLanguage() + "-" + DeskDroidApp.getCountry());
        env.put("ANDROID_DATA", "/data");
        env.put("ANDROID_ROOT", "/system");

        String[] environment = new String[env.size()];
        int index = 0;
        for (Map.Entry<String, String> e : env.entrySet()) {
            environment[index++] = e.getKey() + "=" + e.getValue();
        }

        int start = runShell ? 1 : 0;
        String path = new File(bin, executable).getAbsolutePath();
        String[] cmds = new String[arguments.length + start + 1];
        if (runShell) {
            cmds[0] = "/system/bin/sh";
            cmds[1] = path;
        } else {
            cmds[0] = path;
        }
        System.arraycopy(arguments, 0, cmds, start + 1, arguments.length);

        try {
            process = Runtime.getRuntime().exec(cmds, environment, bin);
            new StreamPipe(this.process.getInputStream()).start();
            new StreamPipe(this.process.getErrorStream()).start();
            if (waitFor) {
                this.process.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Process getProcess() {
        return process;
    }

    public abstract void run();

    public static class Builder {
        private Executor executor = new Executor() {
            @Override
            public void run() {
                if (runnable != null) runnable.run();
            }
        };

        private Runnable runnable;

        public Builder setExecutable(String executable) {
            executor.setExecutable(executable);
            return this;
        }

        public Builder setArguments(String arguments) {
            executor.setArguments(arguments.split(" "));
            return this;
        }

        public Builder setArguments(String[] arguments) {
            executor.setArguments(arguments);
            return this;
        }

        public Builder addEnv(String key, String value) {
            executor.addEnv(key, value);
            return this;
        }

        public Builder removeEnv(String key) {
            executor.removeEnv(key);
            return this;
        }

        public Builder waitFor() {
            executor.setWaitFor(true);
            return this;
        }

        public Builder setRunShell(boolean runShell) {
            executor.setRunShell(runShell);
            return this;
        }

        public Builder setRunnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public Executor create() {
            return executor;
        }
    }

    private class StreamPipe extends Thread {
        private final InputStream in;

        private StreamPipe(InputStream paramInputStream) {
            this.in = paramInputStream;
        }

        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    Log.d("Executable-" + executable, line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
