package cli.command;

import mutex.SuzukiTokenMutex;

public class GetCommand implements CLICommand {
    private SuzukiTokenMutex mutex;

    public GetCommand(SuzukiTokenMutex mutex) {
        this.mutex = mutex;
    }
    @Override
    public String commandName() {
        return "get";
    }

    @Override
    public void execute(String args) {
        mutex.get(args);//pozivam komandu da mi dohvati fajl
    }
}
