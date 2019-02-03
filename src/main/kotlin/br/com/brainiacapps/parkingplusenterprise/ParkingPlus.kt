package br.com.brainiacapps.parkingplusenterprise

import br.com.brainiacapps.parkingplusenterprise.model.*
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import okhttp3.*
import java.io.IOException
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit


interface ParkingPlusResult<T> {
    fun onFailure(error: ParkingPlusError)

    fun onSuccess(value: T)
}

interface ParkingPlus {
    fun pay(ticketNumber: String, creditCard: CreditCard, amount: Int,
            promotionCode: Int? = null, parkingLotCode: Int? = null,
            invoices: List<String>? = null,
            result: ParkingPlusResult<PaymentResult>)

    fun listPayments(from: Int = 0, to: Int = 5, parkingLotCode: Int? = null, result: ParkingPlusResult<List<Payment>>)

    fun checkTicket(number: String, parkingLotCode: Int? = null, result: ParkingPlusResult<ParkingTicket>)

    fun refreshTicket(number: String, parkingLotCode: Int? = null, promotionCode: Int? = null,
                      invoices: List<String>? = null, promotionsKind: List<String>? = null,
                      result: ParkingPlusResult<ParkingTicket>)

    fun listPromotions(number: String, parkingLotCode: Int? = null, result: ParkingPlusResult<List<Promotion>>)
}

class ParkingPlusV2(private val url: String, private val apiKey: String,
                    private val udid: String, private val async: Boolean = true) : ParkingPlus {

    private val JSON = MediaType.get("application/json; charset=utf-8")
    private val client = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

    override fun pay(ticketNumber: String, creditCard: CreditCard, amount: Int,
                     promotionCode: Int?, parkingLotCode: Int?,
                     invoices: List<String>?,
                     result: ParkingPlusResult<PaymentResult>) {
        val ipAddress = "0.0.0.0"
        val issuer = creditCard.issuer
        val creditCardHolderName = creditCard.holderName
        val transactionId = UUID.randomUUID().toString()
        var builder = HttpUrl.parse(url)?.newBuilder()
                ?.addPathSegment("pagamento")
                ?.addQueryParameter("apiKey", (ticketNumber + udid + ipAddress
                        + issuer + creditCardHolderName + transactionId + apiKey).sha1())
        if (parkingLotCode != null) {
            builder = builder?.addQueryParameter("idGaragem", parkingLotCode.toString())
        }
        val paymentRequest = if (creditCard.encryptedValue != null) {
            PaymentRequest(issuer = issuer,
                    creditCardEncrypted = creditCard.encryptedValue,
                    creditCardCVV = creditCard.verificationValue,
                    creditCardHolderName = creditCardHolderName,
                    creditCardHolderDocument = creditCard.holderDocument,
                    transactionId = transactionId,
                    ticketNumber = ticketNumber,
                    amount = amount,
                    ipAddress = ipAddress,
                    udid = udid,
                    parkingLotCode = parkingLotCode,
                    promotionCode = promotionCode,
                    invoices = invoices)
        } else {
            PaymentRequest(issuer = issuer,
                    creditCardNumber = creditCard.number,
                    creditCardValidity = creditCard.validity,
                    creditCardCVV = creditCard.verificationValue,
                    encryptCreditCard = creditCard.encrypt,
                    creditCardHolderName = creditCardHolderName,
                    creditCardHolderDocument = creditCard.holderDocument,
                    transactionId = transactionId,
                    ticketNumber = ticketNumber,
                    amount = amount,
                    ipAddress = ipAddress,
                    udid = udid,
                    parkingLotCode = parkingLotCode,
                    promotionCode = promotionCode,
                    invoices = invoices)
        }
        val body = RequestBody.create(JSON, klaxon.toJsonString(paymentRequest))
        val request = Request.Builder().url(builder?.build()!!).post(body).build()
        execute(request, result) {
            klaxon.parse(it)!!
        }

    }

    override fun listPayments(from: Int, to: Int, parkingLotCode: Int?, result: ParkingPlusResult<List<Payment>>) {
        var builder = HttpUrl.parse(url)?.newBuilder()
                ?.addPathSegment("pagamentosEfetuados")
                ?.addQueryParameter("inicio", from.toString())
                ?.addQueryParameter("fim", to.toString())
                ?.addQueryParameter("udid", udid)
                ?.addQueryParameter("apiKey", (udid + apiKey).sha1())
        if (parkingLotCode != null) {
            builder = builder?.addQueryParameter("idGaragem", parkingLotCode.toString())
        }
        val request = Request.Builder().url(builder?.build()!!).build()
        execute(request, result) {
            klaxon.parseArray(it)!!
        }
    }

    override fun checkTicket(number: String, parkingLotCode: Int?, result: ParkingPlusResult<ParkingTicket>) {
        var builder = HttpUrl.parse(url)?.newBuilder()
                ?.addPathSegment("ticket")
                ?.addPathSegment(number)
                ?.addQueryParameter("udid", udid)
                ?.addQueryParameter("apiKey", (udid + apiKey).sha1())
        if (parkingLotCode != null) {
            builder = builder?.addQueryParameter("idGaragem", parkingLotCode.toString())
        }
        val request = Request.Builder().url(builder?.build()!!).build()
        execute(request, result) {
            klaxon.parse(it)!!
        }
    }

    override fun refreshTicket(number: String, parkingLotCode: Int?, promotionCode: Int?,
                               invoices: List<String>?, promotionsKind: List<String>?,
                               result: ParkingPlusResult<ParkingTicket>) {
        val refreshRequest = ParkingTicketRefreshRequest(number, udid,
                parkingLotCode, promotionCode, invoices, promotionsKind)
        val builder = HttpUrl.parse(url)?.newBuilder()
                ?.addPathSegment("ticket")
                ?.addQueryParameter("apiKey", (udid + apiKey).sha1())
        val body = RequestBody.create(JSON, klaxon.toJsonString(refreshRequest))
        val request = Request.Builder().url(builder?.build()!!).post(body).build()
        execute(request, result) {
            klaxon.parse(it)!!
        }
    }

    override fun listPromotions(number: String, parkingLotCode: Int?, result: ParkingPlusResult<List<Promotion>>) {
        var builder = HttpUrl.parse(url)?.newBuilder()
                ?.addPathSegment("promocoes")
                ?.addQueryParameter("numeroTicket", number)
                ?.addQueryParameter("udid", udid)
                ?.addQueryParameter("apiKey", (apiKey).sha1())
        if (parkingLotCode != null) {
            builder = builder?.addQueryParameter("idGaragem", parkingLotCode.toString())
        }
        val request = Request.Builder().url(builder?.build()!!).build()
        execute(request, result) {
            klaxon.parseArray(it)!!
        }
    }

    private fun <T : Any> execute(request: Request, result: ParkingPlusResult<T>, parser: (value: String) -> T) {
        val call = client.newCall(request)
        if (async) {
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    result.onFailure(ParkingPlusError(500, e.message))
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    acknowledge(response, parser, result)
                }
            })
        } else {
            val response = call.execute()
            acknowledge(response, parser, result)
        }
    }

    private fun <T : Any> acknowledge(response: Response, parser: (value: String) -> T, result: ParkingPlusResult<T>) {
        val status = response.code()
        when (status) {
            200 -> {
                val t = parser(response.body()?.string()!!)
                result.onSuccess(t)
            }
            in 400..499 -> {
                val error = klaxon.parse<ParkingPlusError>(response.body()?.string()!!)
                result.onFailure(error!!)
            }
            in 500..600 -> {
                val error = if (response.header("Content-Type") == JSON.type()) {
                    klaxon.parse(response.body()?.string()!!)!!
                }
                else {
                    val msg = "Estamos com dificuldades para processar sua solicitação, por favor, " +
                            "certifique-se que esteja conectado a internet e tente novamente caso o " +
                            "problema persista dirija-se ao guichê mais próximo para efetuar o pagamento."
                    ParkingPlusError(500, msg)
                }
                result.onFailure(error)
            }
        }
    }
}

private fun String.sha1(): String {
    return MessageDigest.getInstance("SHA-1")
            .digest(this.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { String.format("%02X", it) }
            .toLowerCase()
}

private fun <T> Klaxon.convert(k: kotlin.reflect.KClass<*>, fromJson: (JsonValue) -> T, toJson: (T) -> String, isUnion: Boolean = false) =
        this.converter(object : com.beust.klaxon.Converter {
            @Suppress("UNCHECKED_CAST")
            override fun toJson(value: Any) = toJson(value as T)

            override fun fromJson(jv: JsonValue) = fromJson(jv) as Any
            override fun canConvert(cls: Class<*>) = cls == k.java || (isUnion && cls.superclass == k.java)
        })

private val klaxon = Klaxon()
        .convert(Promotion.Kind::class, { Promotion.Kind.fromValue(it.string!!) }, { "\"${it.value}\"" })
        .convert(Payment.Kind::class, { Payment.Kind.fromValue(it.string!!) }, { "\"${it.value}\"" })
        .convert(Date::class, { Date(it.int?.toLong() ?: it.longValue!!) }, { "\"${it.time}\"" })
        .convert(Promotion.DiscountType::class, { Promotion.DiscountType.fromValue(it.string!!) }, { "\"${it.value}\"" })
