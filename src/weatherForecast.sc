
theme: /

    state: WeatherForecast
        q!: * (~погода|прогноз погоды) * [в] * {$City * * @duckling.date} *
        q!: * {(~погода|прогноз погоды) * @duckling.date} *
        q!: * (~погода * [(будет|ожидается|намечается|следует ожидать|прогнозируется|ждать|обещают)]) * {[@duckling.date] * [в] * [$City] } *
        q!: * (будет|ожидается|намечается|следует ожидать|прогнозируется|обещают) * [@duckling.date] * { [в] * $City * (дождь|солнечно|пасмурно|снег|туман) } *
        intent!: /weather
        script:
            log("///////// MY LOG "+toPrettyString($parseTree));
            if ($parseTree._City && $parseTree["_duckling.date"]) {
                $session.userCity = $parseTree._City.name;
                $session.lon = $parseTree._City.lon;
                $session.lat = $parseTree._City.lat;
                $session.country = getCountryByCode($parseTree._City.country);
                $session.userDate = getUserDate($parseTree["_duckling.date"]); 
                $session.timezone = $parseTree._City.timezone;
                $reactions.transition("/WeatherForecast/CheсkDate");
            }
            else 
                if ($parseTree["_duckling.date"] || $parseTree["_Date"]) 
                {
                    if ($parseTree["_duckling.date"])
                        $session.userDate = getUserDate($parseTree["_duckling.date"]); 
                    
                    if ($parseTree["_Date"])
                        $session.userDate = getUserDate($parseTree["_Date"]); 
                    
                    $reactions.transition("/WeatherForecast/GetCity");
                } 
                else 
                    if ($parseTree._City) {
                        $session.userCity = $parseTree._City.name;
                        $session.lon = $parseTree._City.lon;
                        $session.lat = $parseTree._City.lat;
                        $session.country = getCountryByCode($parseTree._City.country);
                        $session.timezone = $parseTree._City.timezone;
                        $reactions.transition("/WeatherForecast/GetDate");
                    }
                    else 
                    {
                        $reactions.transition("/WeatherForecast/GetCity");
                    }
            
    
        state: GetCity
            random:
                a: Укажите, пожалуйста, название города, для которого хотите узнать прогноз погоды.
                a: Скажите, пожалуйста, для какого города вы хотетие получить прогноз?
                a: Прогноз для какого города хотите получить?
            
            state: UserCity
                q: * $City *
                q: * @Cities *
                script:
                    if ($parseTree._City) {
                        $session.userCity = $parseTree._City.name;
                        $session.lon = $parseTree._City.lon;
                        $session.lat = $parseTree._City.lat;
                        $session.country = getCountryByCode($parseTree._City.country);
                        $session.timezone = $parseTree._City.timezone;
                    }
                    
                    if ($session.userDate)
                        $reactions.transition("/WeatherForecast/CheсkDate");
                    else
                        $reactions.transition("/WeatherForecast/GetDate");
        
            state: LocalCatchAll || noContex = true
                event: noMatch
                script:
                    $session.stateCounterInARow++
                if: $session.stateCounterInARow < 3
                    random: 
                        a: Извините, не совсем понял вас. Напишите, пожалуйста, название города, чтобы я смог узнать прогноз погоды для него.
                        a: К сожалению, не понял вас. Укажите, пожалуйста, нужный вам город?
                else:
                    a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
                
                    script: 
                        $session.userDate = null
                       
                    go!: /SomethingElse
                
        state: GetDate
            random:
                a: На какую дату требуется прогноз?
                a: Прогноз погоды на какую дату вам нужен?
            
            state: UserDate
                q: * @duckling.date *
                script:
                    if ($parseTree["_duckling.date"]) {
                        $session.userDate = getUserDate($parseTree["_duckling.date"]);
                        $reactions.transition("/WeatherForecast/CheсkDate");
                    }
                    
            state: LocalCatchAll || noContex = true
                event: noMatch
                script:
                    $session.stateCounterInARow ++
                if: $session.stateCounterInARow < 3
                    random: 
                        a: Извините, не совсем понял вас. Напишите, пожалуйста, нужную вам дату.
                        a: К сожалению, не понял вас. Введите, пожалуйста, дату, которая вам нужна.
                else:
                    a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
                
                    script: 
                        $session.userDate = null
                        $session.userCity = null;
                        $session.lat = null;
                        $session.lon = null;
                        $session.country = null;
                    
                    go!: /SomethingElse
          
        state: CheсkDate
            script:
                var date = new Date();
                var userDate = new Date($session.userDate);
                
                $temp.date = new Date($jsapi.dateForZone($session.timezone, "YYYY/MM/dd"));
                
                if (DatesDiff($temp.date, userDate) == 0) {
                    $session.userDate = $temp.date;
                    $reactions.transition("/WeatherForecast/TellWeather");
                }
            
                if (userDatePassed(userDate, date)) {
                    $reactions.transition("/WeatherForecast/ThisDayHasPassed");
                    } 
                    else if (DatesDiff(userDate, date) > 5) {
                        $reactions.transition("/WeatherForecast/ThisDayIsNotComingSoon");
                        } else {
                            $reactions.transition("/WeatherForecast/TellWeather"); 
                            };
        
        state: ThisDayHasPassed
            script:
                $session.stateCounter ++
            if: $session.stateCounter < 3
                script:
                    $session.userDate = null;
                random: 
                    a: К сожалению, я не могу узнать прогноз погоды на период времени в прошлом.
                    a: Я не смогу посмотреть прогноз для прошедшего периода.
                go!: /WeatherForecast/GetDate
            else:
                a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
                
                script: 
                    $session.userDate = null;
                    $session.userCity = null;
                    $session.lat = null;
                    $session.lon = null;
                    $session.country = null;
                    
                go!: /SomethingElse
            
        state: ThisDayIsNotComingSoon
            script:
                $session.stateCounter ++
                
            if: $session.stateCounter < 3
                script:
                    $session.userDate = null;
                random: 
                    a: Мне жаль, но метеорологи и я пока не можем дать такие долгосрочные прогнозы.
                    a: Извините, посмотреть прогноз на такую далекую дату я не смогу.
                go!: /WeatherForecast/GetDate
            else:
                a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
                
                script: 
                    $session.userDate = null;
                    $session.userCity = null;
                    $session.lat = null;
                    $session.lon = null;
                    $session.country = null;
                go!: /SomethingElse
        
        state: TellWeather
            script:
                moment.lang('ru'); 
                $temp.userFormatDate = moment($session.userDate).format('LL');
                openWeatherMapCurrent("metric","ru",$session.lat, $session.lon).then(function (res) {
                    
                    if ((DatesDiff($session.userDate, new Date())  == 0) && testMode()) {
                        var answer = "Смог узнать для вас прогноз: сегодня в "+ capitalize($nlp.inflect($session.userCity, "loct")) + " будет " + setTemperature(res);
                        $reactions.answer(answer);
                    } 
                    else {
                    
                        var answers = [
                            "У меня получилось уточнить: на " + setDateCity($temp.userFormatDate) + " температура воздуха составит "+ setTemperature(res),
                            "Смог узнать для вас прогноз: на " + setDateCity($temp.userFormatDate) + " будет " + setTemperature(res)
                        ];
                        var randomAnswer = answers[$reactions.random(answers.length)];
                        $reactions.answer(randomAnswer);
                    }
                    
                }).catch(function (err) {
                     $reactions.transition("/WeatherForecast/TellWeather/Error");
                });
                
            if: $session.country
                if: $session.userHasTour 
                    go!: /WeatherForecast/SomethingElseForWeather
                else:
                    go!: /WeatherForecast/OfferTour
            else:  
                go!: /WeatherForecast/SomethingElseForWeather
                    
            state: Error
                script:
                    $session.stateCounter++
                if: $session.stateCounter < 3
                    go!: /WeatherForecast/TellWeather
                else:
                    a: Мне очень жаль, но при обращении к сервису, содержащему сведения о погоде, произошла ошибка. Пожалуйста, попробуйте написать мне немного позже. Надеюсь работоспособность сервиса восстановится.
                    script:
                        $session.userDate = null;
                        $session.userCity = null;
                        $session.lat = null;
                        $session.lon = null;
                    go!: /SomethingElse
                
        state: SomethingElseForWeather
            random:
                a: Хотите спросить что-то ещё?
                a: Могу ли я помочь чем-то ещё?
                a: Подскажите, у вас остались ещё вопросы?
            buttons:
                "Узнать прогноз с другими параметрами" -> /WeatherForecast
                "Оформить заявку на подбор тура" -> /TravelRequest
            q: * $noWant * || toState = "/DontHaveQuestions", onlyThisState = true
            
            state: AnotherOne
                q: * $somethingElseForWeather *
                script:
                    if (($parseTree._City) && ($parseTree["_duckling.date"])) {
                        $session.userCity = $parseTree._City.name;
                        $session.lon = $parseTree._City.lon;
                        $session.lat = $parseTree._City.lat;
                        $session.country = getCountryByCode($parseTree._City.country);   
                        $session.userDate = getUserDate($parseTree["_duckling.date"]); 
                        $reactions.transition("/WeatherForecast/CheсkDate");
                        }
                    else 
                        if ($parseTree["_duckling.date"]) {
                            $session.userDate = getUserDate($parseTree["_duckling.date"]); 
                            $reactions.transition("/WeatherForecast/GetCity");
                        }
                        else 
                            if ($parseTree._City) {
                                $session.userCity = $parseTree._City.name;
                                $session.lon = $parseTree._City.lon;
                                $session.lat = $parseTree._City.lat;
                                $session.country = getCountryByCode($parseTree._City.country);
                                $reactions.transition("/WeatherForecast/GetDate");
                            }
                            else 
                                $reactions.transition("/WeatherForecast/GetCity");
                            
            state: Agree
                q: * $yesWant * || fromState = "/WeatherForecast/SomethingElseForWeather/AnotherOne", onlyThisState = true
                script:
                    $session.userCity = null;
                    $session.lon = null;
                    $session.lat = null;
                    $session.country = null;   
                    $session.userDate = null;
                go!: /HowCanIHelpYou
                
            state: LocalCatchAll || noContext = true
                event: noMatch
                script:
                    $session.stateCounterInARow ++;
                if: $session.stateCounterInARow < 3
                    random:
                        a: Извините, не совсем понял. Пожалуйста, подскажите, могу ли я ещё чем-то помочь?
                        a: К сожалению, не смог понять, что вы имеете в виду. Подскажите, остались ли у вас ещё вопросы?
                    buttons:
                        "Узнать прогноз с другими параметрами" -> /WeatherForecast
                        "Оформить заявку на подбор тура" -> /TravelRequest
                else:
                    a: Простите, так и не смог понять, что вы имели в виду.
                    go!: /Goodbye
        
        state: OfferTour
            random:
                a: Хотите оставить заявку на подбор тура в {{ capitalize($nlp.inflect($session.country, "accs")) }}?
                a: Можем составить заявку на подбор идеального тура в {{ capitalize($nlp.inflect($session.country, "accs"))}}. Хотите?
            q: * $yesWant * || toState = "/TravelRequest", onlyThisState = true
            q: * $somethingElseForWeather * || toState = "/WeatherForecast/SomethingElseForWeather/AnotherOne" , onlyThisState = true 
            
            state: Disagree 
                q: * $noWant * || fromState = "/WeatherForecast/OfferTour", onlyThisState = true
                q: * $offerreject * || fromState = "/WeatherForecast/OfferTour", onlyThisState = true
                a: Понял вас!
                script:
                    $session.userCity = null;
                    $session.userDate = null;
                    $session.lat = null;
                    $session.lon = null;
                    $session.country = null;
                a: В таком случае, желаете узнать погоду в другом городе мира?
                q: * $yesWant * || toState = "/WeatherForecast", onlyThisState = true
                q: * $noWant * || toState = "/SomethingElse", onlyThisState = true
                q: * $offerreject * || toState = "/SomethingElse", onlyThisState = true
                q: * (~погода|прогноз погоды) * [в] * {$City * * @duckling.date} * || toState = "/WeatherForecast" , onlyThisState = true 
                q: * {(~погода|прогноз погоды) * @duckling.date} * || toState = "/WeatherForecast" , onlyThisState = true 
                q: * (~погода * [(будет|ожидается|намечается|следует ожидать|прогнозируется|ждать|обещают)]) * {[@duckling.date] * [в] * [$City] } * || toState = "/WeatherForecast" , onlyThisState = true 
                q: * (будет|ожидается|намечается|следует ожидать|прогнозируется|обещают) * [@duckling.date] * { [в] * $City * (дождь|солнечно|пасмурно|снег|туман) } * || toState = "/WeatherForecast" , onlyThisState = true 
                intent: /weather || toState = "/WeatherForecast" , onlyThisState = true 
            
            
                state: LocalCatchAll || noContext = true 
                    event: noMatch || fromState = "/WeatherForecast/OfferTour/Disagree", onlyThisState = true
                    script:
                        $session.stateCounter ++;
                    if: $session.stateCounter < 2
                        a: Простите, не совсем понял. Хотите узнать прогноз погоды для другого города?   
                        go: /WeatherForecast/OfferTour/Disagree
                    else
                        go!: /SomethingElse
            
            state: LocalCatchAll || noContext = true
                event: noMatch
                script:
                    $session.stateCounter ++;
                if: $session.stateCounter < 2
                    a: Извините, не совсем понял вас, вы желаете оставить запрос на подбор путевки в {{ capitalize($nlp.inflect($session.country, "accs"))}}?
                else:
                    script:
                        $session.userCity = null;
                        $session.userDate = null;
                        $session.lat = null;
                        $session.lon = null;
                        $session.country = null;
                    go!: /SomethingElse