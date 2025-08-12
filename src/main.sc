
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
        image: https://media.istockphoto.com/id/511095951/ru/%D1%84%D0%BE%D1%82%D0%BE/%D0%BE%D0%BD-%D0%B7%D0%B4%D0%B5%D1%81%D1%8C-%D1%87%D1%82%D0%BE%D0%B1%D1%8B-%D0%BF%D0%BE%D0%BC%D0%BE%D1%87%D1%8C.jpg?s=2048x2048&w=is&k=20&c=86_eS2vtvuPqNIFl04rO9yg1N7bv9yQMpqIrM0SNOH4=
        script:
            $jsapi.startSession();
            $session.userHasTour = false;
            
        if: $client.name
            random:
                a: {{ capitalize($client.name) }}, здравствуйте! Артур из Just Tour на связи. Рад снова видеть вас в чате!
                a: {{ capitalize($client.name) }}, приветствую! На связи Артур из Just Tour, лучшей в мире туристической компании. Рад снова пообщаться с вами!
        else
           random:
                a: Здравствуйте! Меня зовут Артур, бот-помощник компании Just Tour. Расскажу все о погоде в городах мира и помогу с оформлением заявки на подбор тура.
                a: Приветствую вас! Я Артур, работаю виртуальным ассистентом в Just Tour, лучшем туристическом агентстве. Проинформирую вас о погоде в разных городах и соберу все необходимые данные для запроса на подбор путевки.
        
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
            
    state: AreYouRobot
        intent!: /robot
        random:
            a: Я Артур — бот-помощник компании Just Tour, всегда готов отвечать на ваши вопросы.
            a: Вы общаетесь с Артуром - чат-ботом, разработанным командой Just Tour, чтобы помогать вам. Всегда рад пообщаться с вами!
        go!: /SomethingElse
    
    state: WhatCanYouDo
        intent!: /whatCanYouDo
        random:
            a: Умею рассказывать о погоде в городах мира и составлять заявки на подбор подходящего именно вам путешествия.
            a: С удовольствием расскажу вам о ближайших метеопрогнозах для разных городов и помогу составить запрос на подбор тура.
        go!: /SomethingElse
        
    state: HowCanIHelpYou
        random:
            a: Чем могу помочь?
            a: Что вас интересует?
            a: Подскажите, какой у вас вопрос?
        script:
            $session.stateCounterInARow = 0;
        buttons:
            "Узнать прогноз погоды" -> /WeatherForecast
            "Оформить заявку на подбор тура" -> /TravelRequest
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
            "Узнать прогноз погоды" -> /WeatherForecast
            "Оформить заявку на подбор тура" -> /TravelRequest
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
                    
                buttons:
                    "Узнать прогноз погоды" -> /WeatherForecast
                    "Оформить заявку на подбор тура" -> /TravelRequest
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
            