package br.com.brainiacapps.parkingplusenterprise.model

import com.beust.klaxon.Json
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

data class Invoice(@Json(name = "cnpj") var document: String,
                   @Json(name = "data") var date: Date,
                   @Json(name = "erro") var errorMessage: String,
                   @Json(name = "qrCode") var qrCode: String,
                   @Json(name = "valida") var isValid: Boolean,
                   @Json(name = "valor") var amount: Int)

data class ParkingLot(@Json(name = "idGaragem") val code: Int?,
                      @Json(name = "garagem") val name: String?,
                      @Json(name = "cnpjGaragem") val document: String?,
                      @Json(name = "linkLogoGaragem") val logoLink: String?)

data class ParkingPlusError(@Json(name = "errorCode") val code: Int?,
                            @Json(name = "mensagem") val message: String?)

data class CreditCard(val issuer: String,
                      val validity: String,
                      val number: String? = null,
                      val verificationValue: String? = null,
                      var encryptedValue: String? = null,
                      val holderDocument: String,
                      val holderName: String,
                      val encrypt: Boolean = false)

data class ParkingTicket(
        @Json(name = "numeroTicket") val number: String,
        @Json(name = "tarifa") val fareRaw: Int,
        @Json(name = "tarifaPaga") val farePaidRaw: Int,
        @Json(name = "tarifaSemDesconto") val fareWithoutDiscountRaw: Int,
        @Json(name = "ticketValido") val isValid: Boolean,
        @Json(name = "valorDesconto") val discountRaw: Int,
        @Json(name = "dataConsulta") val dateValidation: Date,
        @Json(name = "dataDeEntrada") val dateCheckIn: Date,
        @Json(name = "dataPermitidaSaida") val dateCheckOutMax: Date? = null,
        @Json(name = "idPromocao") val promotionCode: Int? = null,
        @Json(name = "imagemLink") val imageLink: String? = null,
        @Json(name = "notas") var invoices: List<Invoice> = ArrayList(),
        @Json(name = "permiteRecorrencia") val allowsRecurrence: Boolean? = false,
        @Json(name = "promocaoAtingida") val isPromotionAchieved: Boolean? = false,
        @Json(name = "promocoesDisponiveis") val hasPromotionsAvailable: Boolean? = false,
        @Json(name = "recorrente") val recurrent: Boolean? = false,
        @Json(name = "setor") val sector: String? = null,
        @Json(name = "idGaragem") val parkingLotCode: Int? = 0,
        @Json(name = "garagem") val name: String? = null,
        @Json(name = "cnpjGaragem") val document: String? = null,
        @Json(name = "linkLogoGaragem") val logoLink: String? = null,
        @Json(name = "errorCode") val code: Int? = 0,
        @Json(name = "mensagem") val message: String? = null,
        @Json(name = "cartaoMascarado") var creditCardMasked: String? = null,
        @Json(name = "mensagemValidacao") var creditCardValidationMessage: String? = null,
        @Json(name = "solicitarCodigoSeguranca") var creditCardRequestCVV: Boolean = false
) {
    val fareDueRaw = this.fareRaw - this.farePaidRaw
    val fareDue = this.fareDueRaw.by100()
    val fare = this.fareRaw.by100()
    val fareWithoutDiscount = this.fareWithoutDiscountRaw.by100()
    val farePaid = this.farePaidRaw.by100()
    val discount = this.discountRaw.by100()
    val hasDueValue = this.fareDueRaw > 0.0
    val stayInMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - this.dateCheckIn.time)
    val error = ParkingPlusError(code, message)

}

private fun Int.by100(): Double {
    return this / 100.0
}

data class Payment(@Json(name = "ticket") val ticketNumber: String,
                   @Json(name = "valorPago") val amountPaidRaw: Int,
                   @Json(name = "data") val paidAt: Date,
                   @Json(name = "prepago") val isPrePaid: Boolean,
                   @Json(name = "permanencia") val checkInDate: Date,
                   @Json(name = "permanenciaFim") val checkOutDate: Date,
                   @Json(name = "valorDesconto") val amountDiscountRaw: Int,
                   @Json(name = "cpfCnpj") val payerDocument: String? = null,
                   @Json(name = "idGaragem") val code: Int? = null,
                   @Json(name = "garagem") val name: String? = null,
                   @Json(name = "cnpjGaragem") val document: String? = null,
                   @Json(name = "linkLogoGaragem") val logoLink: String? = null,
                   @Json(name = "codigoAutorizacao") val authorizationCode: String? = null,
                   @Json(name = "nfseCodigoVerificacao") val nfseVerificationCode: String? = null,
                   @Json(name = "nfseNumero") val nfseNUmber: String? = null,
                   @Json(name = "nfseQrCode") val nfseQrCode: String? = null,
                   @Json(name = "nsu") val invoiceNSU: String? = null,
                   @Json(name = "rps") val rps: String? = null,
                   @Json(name = "serieRPS") val serieRPS: String? = null,
                   @Json(name = "tipo") val kind: Kind) {

    val stayInMinutes = TimeUnit.MILLISECONDS.toMinutes(this.checkOutDate.time - this.checkInDate.time)
    val amountPaid = amountPaidRaw.by100()
    val discount = amountDiscountRaw.by100()

    enum class Kind(val value: String) {
        CREDIT_CHARGE("CARGA_CREDITO"),
        DEBIT_CHARGE("CARTAO_DEBITO"),
        TICKET("TICKET");

        companion object {
            fun fromValue(value: String): Kind = when (value) {
                "CARGA_CREDITO" -> CREDIT_CHARGE
                "CARTAO_DEBITO" -> DEBIT_CHARGE
                "TICKET" -> TICKET
                else -> throw IllegalArgumentException()
            }
        }
    }

}

data class PaymentRequest(@Json(name = "bandeira") val issuer: String,
                          @Json(name = "idTransacao") val transactionId: String,
                          @Json(name = "numeroTicket") val ticketNumber: String,
                          @Json(name = "udid") val udid: String,
                          @Json(name = "valor") val amount: Int,
                          @Json(name = "enderecoIp") val ipAddress: String,
                          @Json(name = "cartaoCriptografado") val creditCardEncrypted: String? = null,
                          @Json(name = "cartaoDeCredito") val creditCardNumber: String? = null,
                          @Json(name = "codigoDeSeguranca") val creditCardCVV: String? = null,
                          @Json(name = "criptografarCartao") val encryptCreditCard: Boolean = false,
                          @Json(name = "validade") val creditCardValidity: String? = null,
                          @Json(name = "cpfCnpj") val creditCardHolderDocument: String? = null,
                          @Json(name = "portador") val creditCardHolderName: String? = null,
                          @Json(name = "idGaragem") val parkingLotCode: Int? = null,
                          @Json(name = "idPromocao") val promotionCode: Int? = null,
                          @Json(name = "notas") val invoices: List<String>? = null)

data class PaymentResult(@Json(name = "numeroTicket") val ticketNumber: String,
                         @Json(name = "ticketPago") val success: Boolean,
                         @Json(name = "comprovante") val receipt: String,
                         @Json(name = "dataHoraSaida") val checkOutDate: Date,
                         @Json(name = "dataPagamento") val paymentDate: Date,
                         @Json(name = "cartaoCriptografado") val creditCardEncrypted: String? = null,
                         @Json(name = "rps") val rps: String? = null,
                         @Json(name = "serieRps") val serieRPS: String? = null,
                         @Json(name = "errorCode") val code: Int? = 0,
                         @Json(name = "mensagem") val message: String? = null) {
    val error = ParkingPlusError(code, message)
}

data class ParkingTicketRefreshRequest(@Json(name = "numeroTicket") val ticketNumber: String,
                                       @Json(name = "udid") val udid: String,
                                       @Json(name = "idGaragem") val parkingLotCode: Int? = null,
                                       @Json(name = "idPromocao") val promotionCode: Int? = null,
                                       @Json(name = "notas") val invoices: List<String>? = null,
                                       @Json(name = "tiposPromocao") val promotionsKind: List<String>? = null)

data class Promotion(@Json(name = "systemId") val systemId: Int,
                     @Json(name = "titulo") val title: String,
                     @Json(name = "imagem") val image: String? = null,
                     @Json(name = "validade") val validity: Date,
                     @Json(name = "descricao") val description: String,
                     @Json(name = "regulamento") val regulation: String? = null,
                     @Json(name = "tipoPromocao") val kind: Kind,
                     @Json(name = "tipoDesconto") val discountType: DiscountType,
                     @Json(name = "valorAlvo") val targetValue: Int,
                     @Json(name = "horarioInicio") val timeBegin: Date,
                     @Json(name = "horarioFim") val timeEnd: Date,
                     @Json(name = "valorDesconto") val discountValue: Int,
                     @Json(name = "bandeira") val issuer: String? = null,
                     @Json(name = "nome") val name: String? = null,
                     @Json(name = "exigeAutenticacao") val requiresAuth: Boolean) {

    enum class DiscountType(val value: String) {
        PERCENTAGE("PORCENTAGEM"),
        VALUE("VALOR"),
        PERMANENCE("PERMANENCIA");

        companion object {
            fun fromValue(value: String): DiscountType = when (value) {
                "PORCENTAGEM" -> PERCENTAGE
                "VALOR" -> VALUE
                "PERMANENCIA" -> PERMANENCE
                else -> throw IllegalArgumentException()
            }
        }
    }

    enum class Kind(val value: String) {
        ISSUER("BANDEIRA"),
        COUPON("CUPOM");

        companion object {
            fun fromValue(value: String): Kind = when (value) {
                "BANDEIRA" -> ISSUER
                "CUPOM" -> COUPON
                else -> throw IllegalArgumentException()
            }
        }
    }

}
