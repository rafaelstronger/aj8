
package org.apollo.game.event.encoder;

import org.apollo.game.event.EventEncoder;
import org.apollo.game.event.annotate.EncodesEvent;
import org.apollo.game.event.impl.UpdateItemsEvent;
import org.apollo.game.model.Item;
import org.apollo.net.codec.game.DataOrder;
import org.apollo.net.codec.game.DataTransformation;
import org.apollo.net.codec.game.DataType;
import org.apollo.net.codec.game.GamePacket;
import org.apollo.net.codec.game.GamePacketBuilder;
import org.apollo.net.meta.PacketType;

/**
 * An {@link EventEncoder} for the {@link UpdateItemsEvent}.
 * @author Graham
 */
@EncodesEvent( UpdateItemsEvent.class )
public final class UpdateItemsEventEncoder extends EventEncoder<UpdateItemsEvent>
{

	@Override
	public GamePacket encode( UpdateItemsEvent event )
	{
		GamePacketBuilder builder = new GamePacketBuilder( 53, PacketType.VARIABLE_SHORT );

		Item[] items = event.getItems();

		builder.put( DataType.SHORT, event.getInterfaceId() );
		builder.put( DataType.SHORT, items.length );

		for( int i = 0; i < items.length; i ++ ) {
			Item item = items[ i ];
			int id = item == null ? - 1: item.getId();
			int amount = item == null ? 0: item.getAmount();

			if( amount > 254 ) {
				builder.put( DataType.BYTE, 255 );
				builder.put( DataType.INT, DataOrder.INVERSED_MIDDLE, amount );
			} else {
				builder.put( DataType.BYTE, amount );
			}

			builder.put( DataType.SHORT, DataOrder.LITTLE, DataTransformation.ADD, id + 1 );
		}

		return builder.toGamePacket();
	}

}
