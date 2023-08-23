package commands;

import lombok.AllArgsConstructor;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

@AllArgsConstructor
public class HelpCommand implements Command {
    private final Options options;

    @Override
    public void execute() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("StipulaCompiler", options);
    }
}
