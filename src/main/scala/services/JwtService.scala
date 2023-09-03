package services

import domain.User
import domain.api.JwtContent
import domain.errors.ApiError
import io.netty.handler.ssl.util.SelfSignedCertificate
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm
import pdi.jwt.{ JwtAlgorithm, JwtClaim, JwtZIOJson }
import zio.*
import zio.json.*

import java.security.spec.ECGenParameterSpec
import java.security.{ KeyPair, KeyPairGenerator, PrivateKey, PublicKey }
import java.time.Clock

trait JwtService {
  def encode(user: User): UIO[String]
  def decode(jwtToken: String): IO[ApiError, JwtContent]
}

case class JwtServiceLive(
    privateKey: PrivateKey,
    publicKey: PublicKey,
) extends JwtService {
  given Clock = Clock.systemUTC()

  override def encode(user: User): UIO[String] = {
    val claim =
      JwtClaim(
        JwtContent(user.id, user.username).toJson
      )
        .issuedNow
        .expiresIn(60 * 60 * 24)
    ZIO.succeed(JwtZIOJson.encode(claim, privateKey, JwtAlgorithm.ES512))
  }

  override def decode(jwtToken: String): IO[ApiError, JwtContent] =
    ZIO
      .from(
        JwtZIOJson.decode(
          jwtToken,
          publicKey,
        )
      )
      .flatMap(claims => ZIO.from(claims.content.fromJson[JwtContent]))
      .mapError(_ => ApiError.Unauthorized)

}

object JwtService {

  val live: ULayer[JwtService] = ZLayer.succeed {
    val (privateKey, publicKey) = generateKeys()
    JwtServiceLive(privateKey, publicKey)
  }

  def decode(token: String): ZIO[JwtService, ApiError, JwtContent] =
    ZIO.serviceWithZIO[JwtService](_.decode(token))

  private def generateKeys(): (PrivateKey, PublicKey) = {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC")
    keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"))
    val keyPair          = keyPairGenerator.generateKeyPair
    (keyPair.getPrivate, keyPair.getPublic)
  }

}
