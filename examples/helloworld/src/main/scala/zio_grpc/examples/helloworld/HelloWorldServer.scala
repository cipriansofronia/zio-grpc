package zio_grpc.examples.helloworld

import io.grpc.StatusException
import scalapb.zio_grpc.ServerMain
import scalapb.zio_grpc.ServiceList
import zio._
import zio.stream.ZStream
import zio.Console._

import io.grpc.examples.helloworld.helloworld.ZioHelloworld.Greeter
import io.grpc.examples.helloworld.helloworld.{HelloReply, HelloRequest}

object GreeterImpl extends Greeter {
  def sayHello(
      request: HelloRequest
  ): ZIO[Any, StatusException, HelloReply] =
    printLine(s"Got request: $request").orDie zipRight
      ZIO.succeed(HelloReply(s"Hello, ${request.name}"))
  override def streamSayHello(request: HelloRequest): stream.Stream[StatusException, HelloReply] =
    ZStream.repeatWithSchedule(HelloReply(s"Hello Response, ${request.name}"), Schedule.spaced(1.second))
}

object HelloWorldServer extends ServerMain {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(ConfigProvider.fromMap(Map("zio_grpc.backpressure_queue_size" -> "1000000")))

  override def port: RuntimeFlags = 8088

  def services: ServiceList[Any] = ServiceList.add(GreeterImpl)
}
