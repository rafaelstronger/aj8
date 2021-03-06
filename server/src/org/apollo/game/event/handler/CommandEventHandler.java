
package org.apollo.game.event.handler;

import org.apollo.game.command.Command;
import org.apollo.game.command.CommandDispatcher;
import org.apollo.game.event.EventHandler;
import org.apollo.game.event.annotate.HandlesEvent;
import org.apollo.game.event.impl.CommandEvent;
import org.apollo.game.model.Player;

/**
 * An {@link EventHandler} which dispatches {@link CommandEvent}s.
 * @author Graham
 */
@HandlesEvent( CommandEvent.class )
public final class CommandEventHandler extends EventHandler<CommandEvent>
{

	@Override
	public void handle( Player player, CommandEvent event )
	{
		String str = event.getCommand();
		String[] components = str.split( " " );

		String name = components[ 0 ];
		String[] arguments = new String[ components.length - 1 ];

		System.arraycopy( components, 1, arguments, 0, arguments.length );

		Command command = new Command( name, arguments );

		CommandDispatcher.getInstance().dispatch( player, command );
	}

}
