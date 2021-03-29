package physica.nuclear.common.configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import net.minecraftforge.common.config.Configuration;
import physica.CoreReferences;
import physica.api.core.load.IContent;
import physica.api.core.load.LoadPhase;
import physica.nuclear.PhysicaNuclearPhysics;
import physica.nuclear.common.items.update.ItemUpdateAntimatter;

public class ConfigNuclearPhysics implements IContent {

	public static String			CATEGORY							= "NUCLEAR_PHYSICS";

	public static float				ANTIMATTER_CREATION_SPEED			= 1f;
	public static boolean			ENABLE_PARTICLE_COLLISION			= true;
	public static boolean			ENABLE_PARTICLE_CHUNKLOADING		= true;
	public static float				TURBINE_STEAM_TO_RF_RATIO			= 2f;

	public static HashSet<String>	PROTECTED_WORLDS					= new HashSet<>(Arrays.asList("spawn", "creative"));
	public static HashSet<String>	QUANTUM_ASSEMBLER_BLACKLIST			= new HashSet<>();
	public static boolean			FLIP_BLACKLIST_TO_WHITELIST			= false;

	public static int				URANIUM_ORE_MIN_Y					= 10;
	public static int				URANIUM_ORE_MAX_Y					= 40;
	public static int				URANIUM_ORE_COUNT					= 20;
	public static int				URANIUM_ORE_BRANCH_SIZE				= 3;
	public static int				URANIUM_ORE_HARVEST_LEVEL			= 3;

	public static int				PLASMA_STRENGTH						= 8;
	public static int				DARK_MATTER_USES					= 8;

	public static int				CHEMICAL_BOILER_TICKS_REQUIRED		= 800;
	public static int				CHEMICAL_EXTRACTOR_TICKS_REQUIRED	= 400;
	public static int				GAS_CENTRIFUGE_TICKS_REQUIRED		= 2400;
	public static int				NEUTRON_CAPTURE_TICKS_REQUIRED		= 2847;
	public static int				QUANTUM_ASSEMBLER_TICKS_REQUIRED	= 120;

	@Override
	public void register(LoadPhase phase)
	{
		if (phase == LoadPhase.ConfigRegister)
		{
			Configuration configuration = new Configuration(new File(PhysicaNuclearPhysics.configFolder, "NuclearPhysics.cfg"), CoreReferences.VERSION);
			configuration.load();

			ANTIMATTER_CREATION_SPEED = configuration.getFloat("antimatterCreationSpeed", CATEGORY, ANTIMATTER_CREATION_SPEED, 0.5f, 1.5f, "Speed in blocks per tick required for a particle accelerator particle to explode");

			ENABLE_PARTICLE_COLLISION = configuration.getBoolean("particle_collision_enable", CATEGORY, ENABLE_PARTICLE_COLLISION,
					"Should particle accelerator particles collide with entities and eachother? True to enable; False to disable.");
			ENABLE_PARTICLE_CHUNKLOADING = configuration.getBoolean("particle_chunkloading_enable", CATEGORY, ENABLE_PARTICLE_CHUNKLOADING,
					"Should particle accelerator particles force their current chunk to be loaded? True to enable; False to disable.");
			ItemUpdateAntimatter.FULMINATION_ANTIMATTER_ENERGY_SCALE = configuration.getInt("fulmination_antimatter_energy_scale", CATEGORY, ItemUpdateAntimatter.FULMINATION_ANTIMATTER_ENERGY_SCALE, 1, 3000,
					"Multiplier for an antimatter explosion's energy generation in a fulmination generator");

			String[] protectedWorlds = configuration.getStringList("protected_worlds", CATEGORY, PROTECTED_WORLDS.toArray(new String[0]), "Worlds that are protected from typical explosions and such");
			for (String world : protectedWorlds)
			{
				PROTECTED_WORLDS.add(world.toLowerCase());
			}

			QUANTUM_ASSEMBLER_BLACKLIST = new HashSet<>(
					Arrays.asList(configuration.getStringList("quantum_assembler_blacklist", CATEGORY, QUANTUM_ASSEMBLER_BLACKLIST.toArray(new String[0]), "Items which are blacklisted from use in the quantum assembler")));
			FLIP_BLACKLIST_TO_WHITELIST = configuration.getBoolean("flip_blacklist_to_whitelist", CATEGORY, FLIP_BLACKLIST_TO_WHITELIST, "Enable to switch the quantum assembler blacklist to a whitelist");

			TURBINE_STEAM_TO_RF_RATIO = configuration.getFloat("turbineSteamToRfRatio", CATEGORY, TURBINE_STEAM_TO_RF_RATIO, 0.01f, 100f, "Ratio for turbines to convert one mL of steam into RF");

			URANIUM_ORE_MIN_Y = configuration.getInt("uranium_min_y", CATEGORY, URANIUM_ORE_MIN_Y, 0, 255, "Lowest y level/height that ore can spawn");
			URANIUM_ORE_MAX_Y = configuration.getInt("uranium_max_y", CATEGORY, URANIUM_ORE_MAX_Y, 0, 255, "Highest y level/height that ore can spawn");
			URANIUM_ORE_COUNT = configuration.getInt("uranium_chunk_count", CATEGORY, URANIUM_ORE_COUNT, 1, 100, "Max amount of ore to spawn in each chunk. Actual count per chunk is a mix of randomization and conditions of the chunk itself.");
			URANIUM_ORE_BRANCH_SIZE = configuration.getInt("uranium_branch_size", CATEGORY, URANIUM_ORE_BRANCH_SIZE, 0, 100, "Amount of ore to generate per branch");
			URANIUM_ORE_HARVEST_LEVEL = configuration.getInt("uranium_harvest_level", CATEGORY, URANIUM_ORE_HARVEST_LEVEL, 0, 255,
					"Tool level needed to mine Uranium Ore \n" +
							"*     Wood:    0\n" +
							"*     Stone:   1\n" +
							"*     Iron:    2\n" +
							"*     Diamond: 3\n" +
							"*     Gold:    0");

			PLASMA_STRENGTH = configuration.getInt("plasma_strength", CATEGORY, PLASMA_STRENGTH, 0, 50, "Amount of destruction caused by plasma. Used for a few different calculation but main effect is likelihood/amount of fire left behind.");

			DARK_MATTER_USES = configuration.getInt("dark_matter_uses", CATEGORY, DARK_MATTER_USES, 1, 1000, "The number of items each dark matter cell can be used in a quantum assembler");

			// Would rather have "ticks_required" as a prefix instead of a suffix so all of these would show up consecutively in the config (alphabetically-sorted), leaving as-is for backwards compatibility for the quantum assembler setting.
			CHEMICAL_BOILER_TICKS_REQUIRED = configuration.getInt("chemical_boiler_ticks_required", CATEGORY, CHEMICAL_BOILER_TICKS_REQUIRED, 0, 100000, "Number of ticks required to boil a material into uranium hexafluoride in a Chemical Boiler");
			CHEMICAL_EXTRACTOR_TICKS_REQUIRED = configuration.getInt("chemical_extractor_ticks_required", CATEGORY, CHEMICAL_EXTRACTOR_TICKS_REQUIRED, 0, 100000, "Number of ticks required to extract a chemical in a Chemical Extractor");
			GAS_CENTRIFUGE_TICKS_REQUIRED = configuration.getInt("gas_centrifuge_ticks_required", CATEGORY, GAS_CENTRIFUGE_TICKS_REQUIRED, 0, 100000, "Number of ticks required to separate uranium isotopes from uranium hexafluoride in a Gas Centrifuge");
			NEUTRON_CAPTURE_TICKS_REQUIRED = configuration.getInt("neutron_capture_ticks_required", CATEGORY, NEUTRON_CAPTURE_TICKS_REQUIRED, 0, 100000, "Number of ticks required to turn deuterium into tritium in a Neutron Capture Chamber");
			QUANTUM_ASSEMBLER_TICKS_REQUIRED = configuration.getInt("quantum_assembler_ticks_required", CATEGORY, QUANTUM_ASSEMBLER_TICKS_REQUIRED, 0, 100000, "Number of ticks required to duplicate the template item in a Quantum Assembler");

			configuration.save();
		}
	}
}
