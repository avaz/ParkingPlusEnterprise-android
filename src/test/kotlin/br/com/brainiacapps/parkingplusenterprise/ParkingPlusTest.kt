package br.com.brainiacapps.parkingplusenterprise

import br.com.brainiacapps.parkingplusenterprise.model.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ParkingPlusTest {

    private lateinit var parkingPlus: ParkingPlus

    internal interface DefaultResult<T> : ParkingPlusResult<T> {
        override fun onFailure(error: ParkingPlusError) {
            println("error")
            println(error)
        }
    }

    @BeforeAll
    fun setUp() {
        this.parkingPlus = ParkingPlusV2("https://demonstracao.parkingplus.com.br/servicos/2",
                "wps2@18pofe12g5412", "123", false)
    }

    @Test
    fun testPay() {
        val creditCard = CreditCard(number = "1234123412341234",
                validity = "022022", verificationValue = "123",
                issuer = "VISA", holderDocument = "12345678901", holderName = "JOHN VONDORE",
                encrypt = true)
        parkingPlus.pay("039957052642", creditCard, 240500, result = object : DefaultResult<PaymentResult> {
            override fun onSuccess(value: PaymentResult) {
                println(value)
            }
        })
    }

    @Test
    fun testCheckTicket() {
        parkingPlus.checkTicket("123456789012", result = object : DefaultResult<ParkingTicket> {
            override fun onSuccess(value: ParkingTicket) {
                println(value)
            }
        })
    }

    @Test
    fun testRefreshTicket() {
        parkingPlus.refreshTicket("039957051555", 1, 6, result = object : DefaultResult<ParkingTicket> {
            override fun onSuccess(value: ParkingTicket) {
                println(value)
            }
        })
    }

    @Test
    fun testListPromotions() {
        parkingPlus.listPromotions("039957051555", 1, result = object : DefaultResult<List<Promotion>> {
            override fun onSuccess(value: List<Promotion>) {
                println(value)
            }
        })
    }

    @Test
    fun testListPayments() {
        parkingPlus.listPayments(result = object : DefaultResult<List<Payment>> {
            override fun onSuccess(value: List<Payment>) {
                println(value.first())
                println(value.size)
            }
        })
    }
}