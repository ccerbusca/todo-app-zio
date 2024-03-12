package api.services

import api.JwtContent
import api.errors.ApiError
import db.entities.User
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import zio.*
import zio.json.*

import java.security.spec.ECGenParameterSpec
import java.security.{KeyPairGenerator, PrivateKey, PublicKey}

trait JwtService {
  def encode(user: User): UIO[String]
  def decode(jwtToken: String): IO[ApiError, JwtContent]
}

case class JwtServiceLive(
    privateKey: PrivateKey,
    publicKey: PublicKey,
) extends JwtService {

  override def encode(user: User): UIO[String] =
    for {
      given java.time.Clock <- zio.Clock.javaClock
      claim =
        JwtClaim(JwtContent(user.id, user.username).toJson)
          .issuedNow
          .expiresIn(60 * 60 * 24)

    } yield JwtZIOJson.encode(claim, privateKey, JwtAlgorithm.ES512)

  override def decode(jwtToken: String): IO[ApiError, JwtContent] =
    ZIO
      .from(
        JwtZIOJson.decode(
          jwtToken,
          publicKey,
        )
      )
      .flatMap(claims => ZIO.from(claims.content.fromJson[JwtContent]))
      .orElseFail(ApiError.Unauthorized)

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
