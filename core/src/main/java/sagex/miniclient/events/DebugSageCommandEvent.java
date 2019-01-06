package sagex.miniclient.events;

import sagex.miniclient.SageCommand;

public class DebugSageCommandEvent {
    public final SageCommand command;

    public DebugSageCommandEvent(SageCommand command) {
        this.command = command;
    }
}
