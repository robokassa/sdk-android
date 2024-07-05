# Robokassa SDK Android
SDK позволяет интегрировать прием платежей через сервис Robokassa в мобильное приложение Android.

## Требования к проекту
Для работы Robokassa SDK необходимо:
- Android версии 7.0 и выше (API level 24).

## Подключение SDK
### Общая информация
Для работы с SDK вам понадобятся:

- MerchantLogin - идентификатор (логин) магазина
- Password #1 – пароль для подписи запросов к сервису
- Password #2 – пароль для подписи запросов к сервису

Данные можно найти в личном кабинете (ЛК) Robokassa.

### Подключение зависимостей
Для подключения SDK добавьте в [build.gradle][build-config] вашего проекта следующие зависимости:
```groovy
implementation 'example'
```

## Проведение платежей
Процесс платежа состоит из 2-х этапов: вызова платежного окна Robokassa с заданными параметрами и затем, если требуется, осуществления дополнительного запроса к сервису Robokassa для необходимого действия - отмены или подтверждения отложенного платежя или проведения повторной оплаты.

### Вызов платежного окна
Чтобы настроить платежное окно для проведения платежа, требуется:

1. Создать объект [PaymentParams](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/params/PaymentParams.kt), который включает в себя:

   - данные о заказе [OrderParams](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/params/OrderParams.kt)
   
   - данные о покупателе [CustomerParams](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/params/CustomerParams.kt)
   
   - данные о внешнем виде страницы оплаты [ViewParams](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/params/ViewParams.kt)

```kotlin
    val paymentParams =
    PaymentParams().setParams {
        orderParams {                           // данные заказа
            invoiceId = 12345                   // номер заказа в системе продавца
            orderSum = 200.50			        // сумма заказа
            isRecurrent = false                 // флаг определяющий является ли платеж повторяющимся
            isHold = false                      // флаг определяющий является ли платеж отложенным
            description = "Оплата по заказу"    // описание, показываемое покупателю в платежном окне
            receipt = Receipt                   // объект фискального чека
        }
        customerParams {                        // данные покупателя
            culture = Culture.RU                // язык интерфейса
            email = "john@doe.com"              // электронная почта покупателя для отправки уведомлений об оплате
        }
        viewParams {
            toolbarText = "Рекуррентный платеж" // заголовок окна оплаты
        }
    }.also {
        it.setCredentials(MERCHANT_LOGIN, PASSWORD_1, PASSWORD_2)
    }
```

2. Зарегистрировать контракт [RobokassaPayLauncher.Contract](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/pay/RobokassaPayLauncher.kt), и вызвать [ActivityResultLauncher.launch](https://developer.android.com/reference/androidx/activity/result/ActivityResultLauncher#launch(kotlin.Any))

```kotlin
    val payProcessLauncher = registerForActivityResult(RobokassaPayLauncher.Contract) {
        when(it) {
            is RobokassaPayLauncher.Canceled -> {
                // платеж прерван пользователем
            }
            is RobokassaPayLauncher.Error -> {
                // во время проведения платежа произошла ошибка
            }
            is RobokassaPayLauncher.Success -> {
                // платеж выполнен успешно
            }
        }
    }
    payProcessLauncher.launch(RobokassaPayLauncher.StartPay(paymentParams))
```

3. Результат платежа вернется в ActivityResultCallback:

   - при успешно завершенном платеже возвращается [RobokassaPayLauncher.Success](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/pay/RobokassaPayLauncher.kt), который содержит в себе:
   
     - invoiceId - номер оплаченного заказа
     
     - resultCode [CheckRequestCode](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/models/CheckPay.kt) - код результата выполнения запроса в платежном окне
     
     - stateCode [CheckPayStateCode](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/models/CheckPay.kt) - код состояния платежа
     
   - при отмене платежа возвращается RobokassaPayLauncher.Canceled
   
   - при неуспешном платеже в ответ приходит [RobokassaPayLauncher.Error](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/pay/RobokassaPayLauncher.kt), внутри которого находятся:
   
     - error - Throwable
     
     - resultCode [CheckRequestCode](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/models/CheckPay.kt) - код результата выполнения запроса в платежном окне
     
     - stateCode [CheckPayStateCode](https://github.com/robokassa/sdk-adnroid/src/main/Robokassa_Library/src/main/java/com/robokassa/library/models/CheckPay.kt) - код состояния платежа
     
     - desc - текстовое описание ошибки
     
### Дополнительные запросы
Если в платежном окне был вызван не обычный платеж, а холдирование средств или рекуррентный платеж, то далее потребуется вызвать дополнительный метод SDK.

1. При выполнении отложенного платежа (холдировании) необходимо вызвать либо отмену, либо подтверждение платежа
   
```kotlin
    // Подтверждение отложенного платежа, в качестве paymentParams рекомендуется использовать объект, созданный на этапе вызова платежного окна
    val pa = PaymentAction.init()
    pa.confirmHold(paymentParams)
    lifecycleScope.launch {
        pa.state.collect { ps ->
            if (ps is PayActionState) {
                if (ps.success) {
                    // платеж успешно подтвержден
                } else {
                    // операция завершена с ошибкой
                }
            }
        }
    }
    
    // Отмена отложенного платежа, в качестве paymentParams рекомендуется использовать объект, созданный на этапе вызова платежного окна
    val pa = PaymentAction.init()
    pa.cancelHold(paymentParams)
    lifecycleScope.launch {
        pa.state.collect { ps ->
            if (ps is PayActionState) {
                if (ps.success) {
                    // платеж успешно отменен
                } else {
                    // операция завершена с ошибкой
                }
            }
        }
    }
```
   
2. При выполнении рекуррентного платежа можно вызвать повторный платеж с прежними параметрами
   
```kotlin
    // Совершение повторного платежа
    val paymentParams =
    PaymentParams().setParams {
        orderParams {                           // данные заказа
            invoiceId = 12345                   // номер заказа в системе продавца
            previousInvoiceId = 55              // номер первичного заказа, созданного с флагом isRecurrent = true
            orderSum = 200.50			        // сумма заказа
        }
    }.also {
        it.setCredentials(MERCHANT_LOGIN, PASSWORD_1, PASSWORD_2)
    }
    val pa = PaymentAction.init()
    pa.payRecurrent(paymentParams)
    lifecycleScope.launch {
        pa.state.collect { ps ->
            if (ps is PayRecurrentState) {
                if (ps.success) {
                    // повторный платеж успешно инициирован на стороне Robokassa
                } else {
                    // операция завершена с ошибкой
                }
            }
        }
    }
```
