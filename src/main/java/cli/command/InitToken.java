package cli.command;


import app.AppConfig;
import mutex.DistributedMutex;
import mutex.SuzukiTokenMutex;

public class InitToken implements CLICommand {

    private DistributedMutex mutex;

    public InitToken(DistributedMutex mutex) {
        this.mutex = mutex;
    }

    @Override
    public String commandName() {
        return "init_token_mutex";
    }

    @Override
    public void execute(String args) {
        if (mutex != null && mutex instanceof SuzukiTokenMutex) {
            ((SuzukiTokenMutex)mutex).sendTokenForward(); //saljemo token 1
        } else {
            AppConfig.timestampedErrorPrint("Doing init token mutex on a non-token mutex: " + mutex);
        }

    }
}
