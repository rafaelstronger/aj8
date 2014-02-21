
package org.apollo.game.model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apollo.Service;
import org.apollo.fs.IndexedFileSystem;
import org.apollo.fs.parser.ItemDefinitionParser;
import org.apollo.fs.parser.MobDefinitionParser;
import org.apollo.game.model.def.EquipmentDefinition;
import org.apollo.game.model.def.ItemDefinition;
import org.apollo.game.model.def.MobDefinition;
import org.apollo.game.task.Task;
import org.apollo.game.task.TaskScheduler;
import org.apollo.io.EquipmentDefinitionParser;
import org.apollo.util.CharacterRepository;

/**
 * The world class is a singleton which contains objects like the {@link CharacterRepository} for
 * players and mobs. It should only contain
 * things relevant to the in-game world and not classes which deal with I/O and
 * such (these may be better off inside some custom {@link Service} or other
 * code, however, the circumstances are rare).
 * @author Graham
 */
public final class World
{

	/**
	 * The logger for this class.
	 */
	private final Logger logger = Logger.getLogger( getClass().getName() );

	/**
	 * The file system
	 */
	private IndexedFileSystem fileSystem;

	/**
	 * The world.
	 */
	private static final World WORLD = new World();

	/**
	 * Represents the different status codes for registering a player.
	 * @author Graham
	 */
	public enum RegistrationStatus {

		/**
		 * Indicates the world is full.
		 */
		WORLD_FULL,

		/**
		 * Indicates that the player is already online.
		 */
		ALREADY_ONLINE,

		/**
		 * Indicates that the player was registered successfully.
		 */
		OK
	}


	/**
	 * Gets the world.
	 * @return The world.
	 */
	public static World getWorld()
	{
		return WORLD;
	}

	/**
	 * The {@link CharacterRepository} of {@link Mob}s.
	 */
	private final CharacterRepository<Mob> mobRepository = new CharacterRepository<Mob>( WorldConstants.MAXIMUM_MOBS );

	/**
	 * The {@link CharacterRepository} of {@link Player}s.
	 */
	private final CharacterRepository<Player> playerRepository = new CharacterRepository<Player>( WorldConstants.MAXIMUM_PLAYERS );


	/**
	 * Creates the world.
	 */
	private World()
	{

	}


	/**
	 * Initialises the world by loading definitions from the specified file
	 * system.
	 * @param fileSystem The file system.
	 * @throws IOException if an I/O error occurs.
	 */
	public void init( IndexedFileSystem fileSystem ) throws IOException
	{
		this.fileSystem = fileSystem;

		logger.info( "Loading item definitions..." );
		ItemDefinitionParser itemParser = new ItemDefinitionParser( fileSystem );
		ItemDefinition[] itemDefs = itemParser.parse();
		ItemDefinition.init( itemDefs );
		logger.info( "Done (loaded " + itemDefs.length + " item definitions)." );

		logger.info( "Loading equipment definitions..." );
		int nonNull = 0;
		try( InputStream is = new BufferedInputStream( new FileInputStream( "data/equipment.dat" ) ) ) {
			EquipmentDefinitionParser equipParser = new EquipmentDefinitionParser( is );
			EquipmentDefinition[] equipDefs = equipParser.parse();
			for( EquipmentDefinition def: equipDefs ) {
				if( def != null ) {
					nonNull ++ ;
				}
			}
			EquipmentDefinition.init( equipDefs );
		}
		logger.info( "Done (loaded " + nonNull + " equipment definitions)." );

		logger.info( "Loading mob definitions..." );
		MobDefinitionParser mobParser = new MobDefinitionParser( fileSystem );
		MobDefinition[] mobDefs = mobParser.parse();
		MobDefinition.init( mobDefs );
		logger.info( "Done (loaded " + mobDefs.length + " mob definitions)." );
	}


	/**
	 * Gets the character repository. NOTE: {@link CharacterRepository#add(GameCharacter)} and
	 * {@link CharacterRepository#remove(GameCharacter)} should not be called
	 * directly! These mutation methods are not guaranteed to work in future
	 * releases!
	 * <p>
	 * Instead, use the {@link World#register(Player)} and {@link World#unregister(Player)} methods
	 * which do the same thing and will continue to work as normal in future releases.
	 * @return The character repository.
	 */
	public CharacterRepository<Player> getPlayerRepository()
	{
		return playerRepository;
	}


	/**
	 * Gets the mob repository.
	 * @return The mob repository.
	 */
	public CharacterRepository<Mob> getMobRepository()
	{
		return mobRepository;
	}


	/**
	 * Registers the specified player.
	 * @param player The player.
	 * @return A {@link RegistrationStatus}.
	 */
	public RegistrationStatus register( Player player )
	{
		if( isPlayerOnline( player.getEncodedName() ) ) {
			return RegistrationStatus.ALREADY_ONLINE;
		}

		boolean success = playerRepository.add( player );
		if( success ) {
			logger.info( "Registered player: " + player + " [online=" + playerRepository.size() + "]" );
			return RegistrationStatus.OK;
		} else {
			logger.warning( "Failed to register player (server full): " + player + " [online=" + playerRepository.size() + "]" );
			return RegistrationStatus.WORLD_FULL;
		}
	}


	/**
	 * Registers the specified mob.
	 * @param mob The mob.
	 * @return {@code true} if the mob registered successfully, otherwise {@code false}.
	 */
	public boolean register( Mob mob )
	{
		boolean success = mobRepository.add( mob );
		if( success ) {
			logger.info( "Registered mob: " + mob + " [online=" + mobRepository.size() + "]" );
		} else {
			logger.warning( "Failed to register mob, repository capacity reached: [online=" + mobRepository.size() + "]" );
		}
		return success;
	}


	/**
	 * Checks if the specified player is online.
	 * @param name The players name, as a long
	 * @return {@code true} if so, {@code false} if not.
	 */
	public boolean isPlayerOnline( long name )
	{
		for( Player p: playerRepository ) {
			if( p.getEncodedName() == name ) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Unregisters the specified player.
	 * @param player The player.
	 */
	public void unregister( Player player )
	{
		if( playerRepository.remove( player ) ) {
			logger.info( "Unregistered player: " + player + " [online=" + playerRepository.size() + "]" );
		} else {
			logger.warning( "Could not find player to unregister: " + player + "!" );
		}
	}


	/**
	 * Unregisters the specified {@link Mob}.
	 * @param mob The mob.
	 */
	public void unregister( Mob mob )
	{
		if( mobRepository.remove( mob ) ) {
			logger.info( "Unregistered mob: " + mob + " [online=" + mobRepository.size() + "]" );
		} else {
			logger.warning( "Could not find mob " + mob + " to unregister!" );
		}
	}


	/**
	 * Returns the file system.
	 */
	public IndexedFileSystem getFileSystem()
	{
		return fileSystem;
	}


	/**
	 * Schedules a new task.
	 * @param task The {@link Task}.
	 */
	public void schedule( Task task )
	{
		TaskScheduler.getInstance().schedule( task );
	}


	/**
	 * Calls the {@link TaskScheduler#pulse()} method.
	 */
	public void pulse()
	{
		TaskScheduler.getInstance().pulse();
	}

}
