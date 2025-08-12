
theme: /
    state: TravelRequest
        intent!: /selectTour
        q!: * (~заявка * [на]* тур|тур) * [в|на] * @CodeCounties *
        script:
            log("///////// MY LOG "+toPrettyString($parseTree));
            if ($parseTree._CodeCounties) {
                $reactions.transition("/TravelRequest/Agree");
                }
        random:
            a: Готов помочь вам оформить заявку на подбор тура. Как только я соберу от вас нужные для запроса данные, наш менеджер подберет самые подходящие варианты и свяжется с вами.
            a: Рад помочь с оформлением запроса на подбор тура. Как только мы заполним заявку, наш специалист свяжется с вами, чтобы предложить наиболее подходящие варианты путешествий.
        if: $session.country
            go!: /TravelRequest/AskNumberOfPeople
        else:
            a: Подскажите, вы уже определились с страной прибытия?
            
        state: Agree
            q: * @CodeCounties *
            q: * $yesWant * || fromState = "/TravelRequest", onlyThisState = true
            script: 
                if ($parseTree._CodeCounties) {
                    $session.country = $parseTree._CodeCounties.name;   
                }
                    
            if: $session.country
                a: Отлично, я передам консультанту, что местом пребывания станет {{$session.country}}. А теперь давайте перейдем к указанию оставшихся параметров.
                go!: /TravelRequest/AskNumberOfPeople
            else:
                a: Введите название страны.
                    
            state: Country
                q: * @CodeCounties *
                script: 
                    $session.country = $parseTree._CodeCounties.name;  
                a: Отлично, я передам консультанту, что местом пребывания станет {{$session.country}}. А теперь давайте перейдем к указанию оставшихся параметров.    
                go!: /TravelRequest/AskNumberOfPeople
                    
            state: LocalCatchAll || noContext = true
                event: noMatch
                script:
                    $session.stateCounter ++;
                if: $session.stateCounter < 3
                    random:
                        a: Извините, не совсем понял вас. Назовите, пожалуйста, нужную вам страну.
                        a: К сожалению, не понял вас. Введите название страны для поездки.
                else:
                    script:
                        $session.country = "Не указано";
                    a: Простите! Так и не получилось вас понять. Когда консультант получит заявку, он подберет варианты стран для вас. А теперь давайте перейдем к указанию оставшихся параметров.
                    go!: /TravelRequest/AskNumberOfPeople
               
        state: Disagree
            q: * $noWant * || fromState = "/TravelRequest", onlyThisState = true
            a: Понял вас. В таком случае, когда консультант получит заявку, он подберет варианты стран для вас. А теперь давайте перейдем к указанию оставшихся параметров.
            script:
                $session.country = "Не указано";  
            go!: /TravelRequest/AskNumberOfPeople
                
        state: LocalCatchAll || noContext = true
                event: noMatch
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 3
                    random:
                        a: Извините, не совсем понял вас. Подскажите, вы выбрали страну для путешествия?
                        a: К сожалению, не понял вас. Вы выбрали страну для поездки?
                    go: /TravelRequest
                else:
                    script:
                        $session.country = "Не указано";
                    a: Простите! Так и не получилось вас понять. Когда консультант получит заявку, он подберет варианты стран для вас. А теперь давайте перейдем к указанию оставшихся параметров.
                    go!: /TravelRequest/AskNumberOfPeople
                  
        state: AskNumberOfPeople
            a: Укажите количество человек, которые отправятся в путешествие.
            
            state: Number
                q: * @duckling.number *
                q: * (только я|[я] один) *
                script:
                    if ($parseTree["_duckling.number"] > 0)  {
                        $session.numberOfPeople = $parseTree["_duckling.number"];
                        $reactions.transition("/TravelRequest/AskStartDate");
                    } 
                    else if ($parseTree["pattern"]) {
                        $session.numberOfPeople = 1;
                        $reactions.transition("/TravelRequest/AskStartDate");
                        }
                        
                    if ($parseTree["_duckling.number"] < 0)
                        $reactions.transition("/TravelRequest/AskNumberOfPeople/LocalCatchAll");
                
            state: DontKnow  
                intent: /weDoNotKnow
                script:
                    $session.numberOfPeople = "Не указано";
                    $reactions.transition("/TravelRequest/AskStartDate");
                
            state: LocalCatchAll || noContext = true
                event: noMatch
                script:
                    $session.stateCounterInARow ++;
                if: $session.stateCounterInARow < 3
                    script:
                        if ($parseTree["_duckling.number"]) {
                            $reactions.answer("К сожалению, не могу принять такой ответ. Пожалуйста, введите валидное число людей - оно должно быть больше 0.");
                            }
                        else {
                            var answers = ["Извините, не совсем понял вас. Сколько человек планирует отправиться в поездку?",
                            "К сожалению, не понял вас. Сколько человек поедет в тур?"];
                            var randomAnswer = answers[$reactions.random(answers.length)];
                            $reactions.answer(randomAnswer);
                            }
                else: 
                    go!: /TravelRequest/AskNumberOfPeople/DontKnow

        state: AskStartDate
            a: Еще мне потребуется предполагаемая дата начала поездки. Пожалуйста, напишите ее.
            
            state: Date
                q: * @duckling.date *
                q: * @DateStartingCurrent *
                script:
                
                    if ($parseTree["_duckling.date"]) {
                        $session.startDate = getUserDate($parseTree["_duckling.date"]); 
                    
                        if (userDatePassed($session.startDate, new Date())) {
                            $reactions.transition("/TravelRequest/AskStartDate/LocalCatchAll");
                        }  else {
                            $reactions.transition("/TravelRequest/AskDuration");
                            }
                    }
                    
            state: DontKnow  
                intent: /weDoNotKnow
                script:
                    $session.startDate = "Не указано";
                    $reactions.transition("/TravelRequest/AskDuration");
                
            state: LocalCatchAll
                event: noMatch
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 3
                    script:
                        if ($parseTree["_duckling.date"]) {
                            $reactions.answer("К сожалению, не могу принять такой ответ. Пожалуйста, введите актуальную дату - она не должна быть в прошедшем периоде.");
                            }
                        else {
                            var answers = ["Извините, не совсем понял вас. Какого числа предполагаете выезд?",
                            "К сожалению, не понял вас. На какую дату планируете отправление?"];
                            var randomAnswer = answers[$reactions.random(answers.length)];
                            $reactions.answer(randomAnswer);
                            }
                else:
                    script: 
                        $reactions.transition("/TravelRequest/AskStartDate/DontKnow");
                    
        state: AskDuration
            a: Также укажите, сколько дней будет длиться путешествие.
            
            state: Number
                q: * @duckling.number *
                q: * @duckling.duration *
                script:
                    if ($parseTree["_duckling.duration"]){
                        var days = calcDays($parseTree["_duckling.duration"]);
                        $session.endDate = addDays($session.startDate, days);
                        $session.countDays = days;
                        $reactions.transition("/TravelRequest/AskServices");
                        } else if ($parseTree["_duckling.number"] > 0) {
                        $session.countDays = $parseTree["_duckling.number"];
                        $session.endDate = addDays($session.startDate, $parseTree["_duckling.number"]);
                        $reactions.transition("/TravelRequest/AskServices");
                        } 
                        
                    if ($parseTree["_duckling.number"] < 0)    
                        $reactions.transition("/TravelRequest/AskDuration/LocalCatchAll");
                        
            state: DontKnow  
                intent: /weDoNotKnow
                script:
                    $session.endDate = "Не указано";
                    $reactions.transition("/TravelRequest/AskServices");
                    
            state: LocalCatchAll || noContex = true
                event: noMatch
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 3
                    script:
                        if ($parseTree["_duckling.number"]) {
                            $reactions.answer("К сожалению, не могу принять такой ответ. Пожалуйста, введите валидное число дней - оно должно быть больше 0.");
                            }
                        else {
                            var answers = ["Извините, не совсем понял вас. Сколько дней планируете быть в поездке?",
                            "К сожалению, не понял вас. На какой срок планируете отъезд?"];
                            var randomAnswer = answers[$reactions.random(answers.length)];
                            $reactions.answer(randomAnswer);
                            }
                else:
                    script: 
                        $reactions.transition("/TravelRequest/AskDuration/DontKnow");
            
        state: AskServices
            a: Уточните, пожалуйста, какой пакет услуг вам интересен?
            buttons:
                "Эконом" -> /TravelRequest/AskServices/Package
                "Стандарт" -> /TravelRequest/AskServices/Package
                "VIP" -> /TravelRequest/AskServices/Package
            
            state: Package
                q: *[(давай|можно|хоч*|выбир*)]* @Packages *
                script:
                    if ($parseTree._Packages) {
                        $session.services = $parseTree._Packages.name; 
                    }
                    else {
                        $session.services = $request.query;
                    }
                    
                go!:  /TravelRequest/AskName
                
            state: WhatIsIncluded
                intent: /included
                script:
                    var answer = whatIsIncluded($parseTree._Packages);
                    $reactions.answer(answer);
                go!:  /TravelRequest/AskServices
            
            state: Price
                intent: /prices
                script:
                    if ($parseTree._Packages) {
                        if ($session.numberOfPeople !== "Не указано") {
                            if ($session.endDate !== "Не указано") {
                                $session.personalPrice = $session.numberOfPeople * $parseTree._Packages.perDayOneMan*$session.countDays;
                                
                                var answer = getPrice($parseTree._Packages);
                                $reactions.answer(answer);
                                }
                        } else {
                            var answer = "При оформлении пакета услуг \""+$parseTree._Packages.name+" стоимость составит "+numberWithCommas($parseTree._Packages.perDayOneMan)+ " рублей на одного человека.";
                            $reactions.answer(answer);
                            }
                    } else {
                        var answer = getPrices();
                        $reactions.answer(answer);
                        }
                go!: /TravelRequest/AskServices
            
            state: LocalCatchAll || noContext = true
                event: noMatch
                intent: /weDoNotKnow
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 3
                    script:
                        if ($parseTree["pattern"]) {
                            $reactions.answer("Мне жаль, но без указания пакет услуг я не смогу отправить заявку. Сделайте выбор, пожалуйста.");
                            }
                        else {
                            var answers = ["Извините, не совсем понял вас. Какой пакет услуг вам больше всего подходит?",
                            "К сожалению, не понял вас. Какой пакет услуг выбираете?"];
                            var randomAnswer = answers[$reactions.random(answers.length)];
                            $reactions.answer(randomAnswer);
                            $reactions.buttons([{ text: "Эконом", transition: "/TravelRequest/AskServices/Package" },
                            {text: "Стандарт", transition: "/TravelRequest/AskServices/Package"},
                            {text: "VIP", transition: "/TravelRequest/AskServices/Package"}]);
                            }
                else:
                    script: 
                        var answer = "К сожалению, без выбора пакета заявка не может быть отправлена. Вы можете вернуться к её заполнению позже или связаться с нами по номеру 8 (812) 000-00-00.";
                        $reactions.answer(answer);
                        $reactions.transition("/SomethingElse");
            
        state: AskName
            if: $client.name
                script:
                    $reactions.transition("/TravelRequest/AskPhone");
            else:
                script:
                    if ($context.session.lastState !== "/TravelRequest/AskName/LocalCatchAll"){
                        var answer = "С параметрами заявки почти закончили! Осталось указать контакты, чтобы менеджер смог связаться с вами.";
                        $reactions.answer(answer);
                        }
                
                    var answer = "Введите, пожалуйста, ваше имя.";
                    $reactions.answer(answer);
                
                    
            state: Name
                q: * @pymorphy.name *
                intent: /askName
                script:
                    if ($parseTree["_pymorphy.name"]) {
                        $client.name = capitalize($parseTree["_pymorphy.name"]);
                        } else if ($parseTree["pattern"] && ($parseTree["_Root"] !== "да")) {
                            $session.userName = capitalize($request.query);
                            } 
                    
                go!: /TravelRequest/AskPhone    
            
            state: LocalCatchAll || noContext = true
                event: noMatch
                intent: /weDoNotKnow
                intent: /doNotWantToIndicate
                intent: /forWhat
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 2
                    script:
                        if ($parseTree["pattern"]) {
                            $reactions.answer("Мне жаль, но без указания вашего имени отправить заявку не получится. Укажите его, пожалуйста.");
                            }
                        else {
                            $session.userName = $request.query;
                            $reactions.transition("/TravelRequest/UnusualName");    
                        }
                else:
                    script: 
                        var answer = "К сожалению, без указания вашего имени заявка не может быть отправлена. Вы можете вернуться к ее заполнению позже или связаться с нами по номеру 8 (812) 000-00-00.";
                        $reactions.answer(answer);
                        $reactions.transition("/SomethingElse");       
            
        state: UnusualName   
            a: Как необычно! Подскажите, вы точно хотели указать в качестве своего имени "{{ $request.query }}"?
            script: 
                $session.userName = $request.query;
            q: * $noWant * || toState = "/TravelRequest/AskName", onlyThisState = true
            q: (* $yesWant *|* @pymorphy.name *) || toState = "/TravelRequest/AskName/Name", onlyThisState = true
            event: noMatch || toState = "/TravelRequest/AskName/Name", onlyThisState = true
            
        state: AskPhone
            if: $client.phone
                go!: /TravelRequest/AskComment
            else:
                a: Укажите номер телефона для связи.
                script:
                    $reactions.buttons({ text: "Поделиться контактом", request_contact: true })
                
            state: Phone
                event: telegramSendContact
                q: * @duckling.phone-number *
                q: * {мой номер * * @duckling.phone-number} *
                script:
                    if ($parseTree["_duckling.phone-number"]) {
                        $client.phone_number = $parseTree["_duckling.phone-number"];
                        $reactions.transition("/TravelRequest/AskComment");  
                        }
                    else {
                        $client.phone_number = $request.rawRequest.message.contact.phone_number;
                        $reactions.transition("/TravelRequest/AskComment");  
                        }
                    
            state: LocalCatch || noContext = true
                event: noMatch
                intent: /weDoNotKnow
                intent: /doNotWantToIndicate
                intent: /forWhat
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 3
                    script:
                        if ($parseTree["pattern"]) {
                            $reactions.answer("Мне жаль, но без указания вашего контактного номера одтправить заявку не получится. Укажите его, пожалуйста.");
                            }
                        else {
                            var answers = ["Извините, не совсем понял вас. Для заявки требуется ваш контактный номер, поэтому, пожалуйста, укажите его.",
                            "К сожалению, не смог распознать номер телефона в вашем ответе. Пожалуйста, укажите его."];
                            var randomAnswer = answers[$reactions.random(answers.length)];
                            $reactions.answer(randomAnswer);
                        }
                else:
                    script: 
                        $session.stateCounterInARow = 0;
                        var answer = "К сожалению, без указания вашего номера телефон заявка не может быть отправлена. Вы можете вернуться к ее заполнению позже или связаться с нами по номеру 8 (812) 000-00-00.";
                        $reactions.answer(answer);
                        $reactions.transition("/SomethingElse");      
            
        state: AskComment
            a: Теперь напишите комментарий для менеджера, если это требуется.
            buttons:
                "Не нужно" -> /TravelRequest/AskComment/Disagree
            
            state: Comment
                event: noMatch
                script:
                    $session.userComment = $request.query;
                go!: /TravelRequest/Confirmation
                
            state: Disagree
                intent: /weather
                intent: /doNotWantToIndicate
                intent: /forWhat
                q: * $noWant * || fromState = "/TravelRequest/AskComment", onlyThisState = true
                script:
                    $session.userComment = "Не указано";
                go!: /TravelRequest/Confirmation
    
        state: Confirmation
            script:
                moment.lang('ru');
                $temp.confirmation = confirmation();
                $reactions.answer($temp.confirmation);
                   
            a: Подскажите, вы готовы отправить заявку?
            buttons:
                "Да" -> /TravelRequest/Confirmation/Agree
                "Нет" -> /TravelRequest/Confirmation/Disagree
            
            state: Agree
                intent: /confirmationYes
                script: 
                    var message = mailText();
                    $session.userHasTour = true;
                
                    $temp.mailResult = sendEmail(message);
                
                    if ($temp.mailResult.status === "OK") {
                        $session.userHasTour = true;
                        $reactions.answer("Ваша заявка успешно отправлена! Как только наш менеджер выберет самые подходящие для вас варианты, он обязательно с вами свяжется.");
                        $reactions.transition("/GoodBye");    
                    }
                    else {
                        $reactions.transition("/TravelRequest/Confirmation/Agree/Error"); 
                    }
                
                state: Error
                    script:
                        $session.stateCounter ++;
                    if: $session.stateCounter < 3
                        go!: /TravelRequest/Confirmation/Agree
                    else:
                        script:
                            $session.country = null;
                            $session.numberOfPeople = null;
                            $session.startDate = null;
                            $session.endDate = null;
                            $session.services = null;
                            $session.userName = null;
                            $session.userComment = null;
                            $session.personalPrice = null;
                        
                            var answer = "К сожалению, произошла техническая ошибка при обращении к сервису хранения заявок. Пожалуйста, позвоните по вопросу подбора путевки нам в Just Tour по номеру 8 (812) 000-00-00.";
                            $reactions.answer(answer);
                            $reactions.transition("/SomethingElse");   
                        
            state: Disagree
                intent: /confirmationNo
                a: В таком случае, вы всегда можете вернуться к заполнению заявки повторно или связаться с нами по телефону 8 (812) 000-00-00.
                script:
                    $session.country = null;
                    $session.numberOfPeople = null;
                    $session.startDate = null;
                    $session.endDate = null;
                    $session.services = null;
                    $session.userName = null;
                    $session.userComment = null;
                    $session.personalPrice = null;
                go!: /SomethingElse
            
            state: LocalCatch || noContext = true
                event: noMatch
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 3
                    script:
                        var answers = ["Извините, не совсем понял вас. Хотите отправить эту заявку?",
                        "К сожалению, не смог понять вас. Отправляем эту заявку?"];
                        var randomAnswer = answers[$reactions.random(answers.length)];
                        $reactions.answer(randomAnswer);
                    
                        $reactions.buttons([{ text: "Да", transition: "/TravelRequest/Confirmation/Agree" },
                        {text: "Нет", transition: "/TravelRequest/Confirmation/Disagree"}])
                    
                else:
                    script: 
                        $session.country = null;
                        $session.numberOfPeople = null;
                        $session.startDate = null;
                        $session.endDate = null;
                        $session.services = null;
                        $session.userName = null;
                        $session.userComment = null;
                        $session.personalPrice = null;
                    
                        var answer = "К сожалению, так и не смог понять, что имелось в виду. Вы всегда можете вернуться к заполнению заявки повторно или связаться с нами по номеру 8 (812) 000-00-00.";
                        $reactions.answer(answer);
                        $reactions.transition("/SomethingElse");     