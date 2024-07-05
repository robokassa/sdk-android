package com.robokassa.library.params

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.robokassa.library.errors.RoboSdkException
import com.robokassa.library.models.Currency
import com.robokassa.library.models.Receipt
import java.util.Date

/**
 * Объект данных о заказе.
 */
class OrderParams() : Params(), Parcelable {

    /**
     * Номер счета в магазине. Необязательный параметр, но мы настоятельно рекомендуем его использовать.
     * Значение этого параметра должно быть уникальным для каждой оплаты.
     * Если значение параметра пустое, или равно 0, или параметр вовсе не указан,
     * то при создании операции оплаты ему автоматически будет присвоено уникальное значение.
     */
    var invoiceId: Int = -1

    /**
     * Номер счета первого платежа в серии повторяющихся платежей для рекуррентной оплаты.
     */
    var previousInvoiceId: Int = -1

    /**
     * Требуемая к получению сумма (буквально — стоимость заказа, сделанного клиентом).
     * Сумма должна быть указана в рублях.
     */
    var orderSum: Double = 0.0

    /**
     * Описание покупки, можно использовать только символы английского или русского алфавита,
     * цифры и знаки препинания. Максимальная длина — 100 символов.
     * Эта информация отображается в интерфейсе Robokassa на платежной странице и в личном кабинете.
     */
    var description: String? = null

    /**
     * Предлагаемый способ оплаты. Тот вариант оплаты, который Вы рекомендуете использовать своим покупателям
     * (если не задано, то по умолчанию открывается оплата Банковской картой).
     * Если параметр указан, то покупатель при переходе на сайт Robokassa попадёт на страницу оплаты
     * с выбранным способом оплаты.
     */
    var incCurrLabel: String? = null

    /**
     * Этот параметр показывает, что выставление данного счета будет повторяющимся.
     */
    var isRecurrent: Boolean = false

    /**
     * Этот параметр показывает, что необходимо предварительное холдирование денежных средств.
     */
    var isHold: Boolean = false

    /**
     * Способ указать валюту, в которой магазин выставляет стоимость заказа.
     * Этот параметр нужен для того, чтобы избавить магазин от самостоятельного пересчета по курсу.
     * Является дополнительным к обязательному параметру orderSum. Если этот параметр присутствует,
     * то переданное значение будет автоматически сконвертировано в Российские рубли.
     */
    var outSumCurrency: Currency? = null

    /**
     * Срок действия счета. Этот параметр необходим, чтобы запретить пользователю оплату позже
     * указанной магазином даты при выставлении счета.
     */
    var expirationDate: Date? = null

    /**
     * Фискальный чек. Передаётся вместе со всеми остальными параметрами для инициализации платежа,
     * а так же этот параметр, если передаётся, обязательно должен быть включен в подсчёт контрольной суммы.
     * В этом параметре передается информация о перечне товаров/услуг, количестве, цене,
     * налговой ставке и ставке НДС по каждой позиции.
     */
    var receipt: Receipt? = null

    @Suppress("DEPRECATION")
    private constructor(parcel: Parcel) : this() {
        parcel.run {
            invoiceId = readInt()
            previousInvoiceId = readInt()
            orderSum = readDouble()
            description = readString()
            incCurrLabel = readString()
            isRecurrent = readByte().toInt() != 0
            isHold = readByte().toInt() != 0
            outSumCurrency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readSerializable(Currency::class.java.classLoader, Currency::class.java)
            } else {
                readSerializable() as? Currency
            }
            expirationDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readSerializable(Date::class.java.classLoader, Date::class.java)
            } else {
                readSerializable() as? Date
            }
            receipt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readSerializable(Receipt::class.java.classLoader, Receipt::class.java)
            } else {
                readSerializable() as? Receipt
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.run {
            writeInt(invoiceId)
            writeInt(previousInvoiceId)
            writeDouble(orderSum)
            writeString(description)
            writeString(incCurrLabel)
            writeByte((if (isRecurrent) 1 else 0).toByte())
            writeByte((if (isHold) 1 else 0).toByte())
            writeSerializable(outSumCurrency)
            writeSerializable(expirationDate)
            writeSerializable(receipt)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(RoboSdkException::class)
    override fun checkRequiredFields() {
        check(orderSum > 0.0) { "Order Sum value cannot be less than 0" }
        check((description?.length ?: 0) > 100) { "Description value cannot be 100 chars longer" }
    }

    companion object CREATOR : Parcelable.Creator<OrderParams> {
        override fun createFromParcel(parcel: Parcel): OrderParams {
            return OrderParams(parcel)
        }

        override fun newArray(size: Int): Array<OrderParams?> {
            return arrayOfNulls(size)
        }
    }
}