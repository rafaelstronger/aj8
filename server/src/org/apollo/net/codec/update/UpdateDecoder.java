
package org.apollo.net.codec.update;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.apollo.fs.FileDescriptor;
import org.apollo.net.codec.update.OnDemandRequest.Priority;

/**
 * A {@link ByteToMessageDecoder} for the 'on-demand' protocol.
 * @author Graham
 */
public final class UpdateDecoder extends ByteToMessageDecoder
{

	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out )
	{
		if( in.readableBytes() < 4 ) {
			return;
		}

		int type = in.readUnsignedByte() + 1;
		int file = in.readUnsignedShort();
		int priorityId = in.readUnsignedByte();
		if( priorityId != 10 ) {
			Priority priority = Priority.valueOf( priorityId );
			FileDescriptor descriptor = new FileDescriptor( type, file );

			out.add( new OnDemandRequest( descriptor, priority ) );
		}
	}

}
