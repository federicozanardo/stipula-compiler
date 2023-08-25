import commands.CompileCommand;
import commands.HelpCommand;
import compiler.module.CompilerModule;
import lcp.lib.communication.module.channel.ModelChannelUtils;
import lcp.lib.exceptions.communication.module.RegisterModuleException;
import lcp.lib.exceptions.communication.module.channel.RegisterChannelException;
import lombok.extern.slf4j.Slf4j;
import modules.StipulaCompilerModule;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import storage.core.lib.exceptions.database.DatabaseException;
import storage.core.lib.module.StorageModule;
import storage.module.LevelDBStorageModule;
import storage.module.services.asset.AssetsStorageService;
import storage.module.services.ownership.OwnershipsStorageService;

@Slf4j
public class Main {
    public static void main(String[] args) {
        BasicConfigurator.configure();

        StipulaCompilerModule stipulaCompilerModule = new StipulaCompilerModule();
        CompilerModule compilerModule = new CompilerModule(LevelDBStorageModule.class.getSimpleName());
        LevelDBStorageModule storageModule = new LevelDBStorageModule();

        // TODO: remove it
        /*try {
            seed(assetsStorageService, ownershipsStorageService);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }*/
        // storageModule.seed();

        // Setup channels
        setupChannels(stipulaCompilerModule, compilerModule, storageModule);

        Options options = new Options();
        options.addOption("h", "help", false, "Print help");
        options.addOption("i", "input", true, "Specify the contract file to compile (.stipula)");
        options.addOption("o", "output", true, "Specify the path where to save the compiled");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpCommand command = new HelpCommand(options);
                command.execute();
            }

            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                CompileCommand command = new CompileCommand(cmd.getOptionValue("i"), cmd.getOptionValue("o"), stipulaCompilerModule);
                command.execute();
            }
        } catch (ParseException e) {
            System.out.println("Error parsing command line: " + e.getMessage());
        }
    }

    private static void setupChannels(StipulaCompilerModule stipulaCompilerModule, CompilerModule compilerModule, StorageModule storageModule) {
        try {
            ModelChannelUtils.setupChannel(stipulaCompilerModule, compilerModule);
            ModelChannelUtils.setupChannel(compilerModule, storageModule);
        } catch (RegisterModuleException | RegisterChannelException e) {
            // TODO: handle it
            throw new RuntimeException(e);
        }
    }

    private static void seed(AssetsStorageService assetsStorageService, OwnershipsStorageService ownershipsStorageService) throws DatabaseException {
        assetsStorageService.seed();
        ownershipsStorageService.seed();
    }
}
