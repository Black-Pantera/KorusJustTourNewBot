require: patterns.sc

init: 
    var SESSION_TIMEOUT_MS = 20200000;    //86400000; // Один день
    
    bind("onAnyError", function($context) {
        var answers = [
            "Извините, произошла техническая ошибка. Специалисты обязательно изучат её и возьмут в работу. Пожалуйста, напишите в чат позже.",
            "Простите, произошла ошибка в системе. Наши специалисты обязательно её исправят."
        ];
        var randomAnswer = answers[$reactions.random(answers.length)];
        $reactions.answer(randomAnswer);
           
        $reactions.buttons({ text: "В главное меню", transition: "/Start" })
    }); 
  
theme: /
    
    state: Start
        q!: $regex</start>
        q!: старт
        q!: * $hello *
        script:
            $jsapi.startSession();
        random: 
            a: Добрый день! В этом чате мы подскажем, как работать в личном кабинете.
        go!: /Operator

    state: GlobalCatchAll || noContext = true
        event!: noMatch
        a: Кажется, этот вопрос не в моей компетенции. Но я постоянно учусь новому, и, надеюсь, совсем скоро научусь отвечать и на него.
        go!: /GoodBye
         
        state: LocalCatchAll || noContex = true
            event: noMatch
            a: Кажется, этот вопрос не в моей компетенции. Но я постоянно учусь новому, и, надеюсь, совсем скоро научусь отвечать и на него.
            go!: /GoodBye
                
    state: DontHaveQuestions
        q!: * $noQuestions *
        random:
            a: Вас понял!
            a: Хорошо!
            a: Понял!
        go!: /GoodBye    
              
    state: GoodBye
        intent!: /goodBye
        script:
            $jsapi.stopSession();
        random:
            a: Всего доброго!
            a: Всего вам доброго!
            a: Всего доброго, до свидания!
            
    state: Operator
        intent!: /ПереводНаОператора
        TransferToOperator: 
            titleOfCloseButton = Переключить обратно на бота
            messageBeforeTransfer = Подскажите, какой у вас вопрос?
            ignoreOffline = true
            messageForWaitingOperator = Вам ответит первый освободившийся оператор.
            noOperatorsOnlineState = /Operator/Error
            dialogCompletedState = /SomethingElse
            sendMessageHistoryAmount = 5
            sendMessagesToOperator = true
            messageBeforeTransferHtml = 
            prechatAttributes = {}
            messageForWaitingOperatorHtml = 
            actions = 
            htmlEnabled = false
            destination = 

        state: Error
            a: К сожалению, все операторы сейчас заняты. Мы обязательно свяжемся с вами позже.
            go!: /SomethingElse
            