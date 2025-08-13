
require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: city/city.sc
    module = sys.zb-common  
require: name/name.sc
    module = sys.zb-common
require: dateTime/moment.min.js
    module = sys.zb-common   
require: common.js
    module = sys.zb-common
  
require: funcs.js
require: patterns.sc
require: weatherForecast.sc
require: travelRequest.sc

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
    
    bind("preProcess", function($context) {
        if (!$context.session.stateCounter) {
            $context.session.stateCounter = 0;
        }
        
        if (!$context.session.stateCounterInARow) {
            $context.session.stateCounterInARow = 0;
        }
        
        if ($context.session.lastActiveTime) {
            var interval = $jsapi.currentTime() - $context.session.lastActiveTime;
            if (interval > SESSION_TIMEOUT_MS) $jsapi.startSession();
        }
    });
        
    bind("postProcess", function($context) {
        $context.session.lastState = $context.currentState;
        $context.session.lastActiveTime = $jsapi.currentTime();
        
        if (checkState($context.currentState)) { 
            $context.session.stateCounter = 0;
            $context.session.stateCounterInARow = 0;
        }
    });
  
theme: /
    
    state: Start
        q!: $regex</start>
        q!: старт
        q!: * $hello *
        script:
            $jsapi.startSession();
            $session.userHasTour = false;
            
        if: $client.name
            random:
                a: {{ capitalize($client.name) }}, здравствуйте! Я бот компании Х. Помогу вам разобраться с работой в личном кабинете.
                a: {{ capitalize($client.name) }}, приветствую! Я бот компании Х. Подскажу вам, как работать в личном кабинете.
        else
           random:
                a: Здравствуйте! Я бот компании Х. Помогу вам разобраться с работой в личном кабинете.
                a: Приветствую вас! Я бот компании Х. Подскажу вам, как работать в личном кабинете.
        go!: /HowCanIHelpYou
            
    state: GlobalCatchAll || noContext = true
        event!: noMatch
        script:
            $session.stateCounterInARow++
                
        if: $session.stateCounterInARow < 3
            random: 
                a: Прошу прощения, не совсем вас понял. Попробуйте, пожалуйста, переформулировать ваш вопрос.
                a: Простите, не совсем понял. Что именно вас интересует?
                a: Простите, не получилось вас понять. Переформулируйте, пожалуйста.
                a: Не совсем понял вас. Пожалуйста, попробуйте задать вопрос по-другому.
        else:
            a: Кажется, этот вопрос не в моей компетенции. Но я постоянно учусь новому, и, надеюсь скоро научусь отвечать и на него.
                
            script: 
                $session.stateCounterInARow = 0
                    
            go!: /SomethingElse
    
    state: WhatCanYouDo
        intent!: /whatCanYouDo
        random:
            a: Могу помочь разобраться с работой в личном кабинете.
            a: Помогаю с работой в личном кабинете.
        go!: /SomethingElse
        
    state: HowCanIHelpYou
        random:
            a: Чем могу помочь?
            a: Что вас интересует?
            a: Подскажите, какой у вас вопрос?
        script:
            $session.stateCounterInARow = 0;
        q: * $noQuestions * || toState = "/DontHaveQuestions", onlyThisState = true
                
        state: LocalCatchAll || noContex = true
            event: noMatch
            
            script:
                $session.stateCounterInARow ++;
                
            if: $session.stateCounterInARow < 3
                random: 
                    a: Извините, не совсем понял. Пожалуйста, подскажите, могу ли я чем-то вам помочь?
                    a: К сожалению, не смог понять, что вы имеете в виду. Подскажите, что вас интересует?
            else:
                script: 
                    $session.stateCounterInARow = 0;
                a: Кажется, этот вопрос не в моей компетенции. Но я постоянно учусь новому, и, надеюсь, совсем скоро научусь отвечать и на него.
                go!: /SomethingElse
           
    state: SomethingElse  
        random:
            a: Хотите спросить что-то еще?
            a: Могу ли я помочь чем-то еще?
            a: Подскажите, у вас остались ещё вопросы?
        buttons:
        q: * $noWant * || toState = "/DontHaveQuestions", onlyThisState = true
        q: * $yesWant * || toState = "/HowCanIHelpYou", onlyThisState = true
        
        state: LocalCatchAll || noContex = true
            event: noMatch
            
            script:
                $session.stateCounterInARow++
                
            if: $session.stateCounterInARow < 3
                random: 
                    a: Извините, не совсем понял. Пожалуйста, подскажите, могу ли я чем-то помочь?
                    a: К сожалению, не смог понять, что вы имеете в виду. Подскажите, что вас интересует?
                    
            else:
                a: Простите, так и не смог понять, что вы имели ввиду.
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
            messageBeforeTransfer = Подождите немного. Соединяю вас со специалистом.
            ignoreOffline = true
            messageForWaitingOperator = Вам ответит первый освободившийся оператор.
            noOperatorsOnlineState = /Operator/Error
            dialogCompletedState = /SomethingElse
            sendMessageHistoryAmount = 5
            sendMessagesToOperator = true
            
        state: Error
            a: К сожалению, все операторы сейчас заняты. Мы обязательно свяжемся с вами позже.
            go!: /SomethingElse
            