package io.finagler.grpc.core.runtime

import com.google.protobuf._

class ProtobufCodec[T <: MessageLite](protoType: T) extends Codec[T] {

  override def encode(message: T, pbos: CodedOutputStream): Unit = message.writeTo(pbos)

  override def decode: CodedInputStream => T = {
    pbis => protoType.newBuilderForType().mergeFrom(pbis).build().asInstanceOf[T]
  }

  override def sizeOf(message: T): Int = message.getSerializedSize
}
