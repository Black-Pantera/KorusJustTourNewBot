function checkState(st) {
    var states = ["/SomethingElse", "/GoodBye", "/WeatherForecast/SomethingElseForWeather", 
    "/WeatherForecast/GetCity", "/WeatherForecast/GetDate", "/WeatherForecast/TellWeather", "/WeatherForecast/SomethingElseForWeather", "/WeatherForecast/OfferTour",
    "/TravelRequest/AskNumberOfPeople", "/TravelRequest/AskNumberOfPeople/DontKnow", "/TravelRequest/AskStartDate", "/TravelRequest/AskStartDate/DontKnow", "/TravelRequest/AskDuration",
    "/TravelRequest/AskDuration/DontKnow", "/TravelRequest/AskServices", "/TravelRequest/AskName", "/TravelRequest/AskPhone", 
    "/TravelRequest/AskComment", "/TravelRequest/UnusualName", "/TravelRequest/Confirmation", "/TravelRequest/Confirmation/Agree", "/TravelRequest/Confirmation/Agree/Error"];
    return (states.indexOf(st) > -1);  
}

function getWeekNumber(d) {
    d = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
    d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay()||7));
    var yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));
    var weekNo = Math.ceil(( ( (d - yearStart) / 86400000) + 1)/7);
    return [d.getUTCFullYear(), weekNo];
}

function DatesDiff(date1, date2) {
   var diffTime = Math.abs(date2 - date1);
   var diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)); 
   return diffDays;
}

function addDays(date, days) {
    var dt = new Date(date);
    var newDate = new Date(dt.getTime() + days * 24 * 60 * 60 * 1000);
    return newDate;
}

function userDatePassed(date1, date2){
    var userDate = date1.setHours(0,0,0,0);
    var date = date2.setHours(0,0,0,0);
    
    return userDate < date;
}

function calcDays(dt) {
    var sec = dt.normalized.value;
    return Math.floor(sec/86400);
}

function getUserDate(dt){
    return new Date(dt.year + "/"+ dt.month + "/"+ dt.day);
}

function setTemperature(res) {
    if ((Math.floor(res.main.temp) == 1) ||  (Math.floor(res.main.temp) == -1))
        return Math.floor(res.main.temp)+ " "+ $nlp.conform("градус", Math.floor(res.main.temp)) +" по Цельсию.";
    else
        return Math.floor(res.main.temp)+ " "+ $nlp.conform("градусы", Math.floor(res.main.temp)) +" по Цельсию.";
}

function setDateCity(dt) {
    var $session = $jsapi.context().session;
    return dt + " в "+ capitalize($nlp.inflect($session.userCity, "loct"))
}

function whatIsIncluded(pc) {
    if (pc) {
        var answer = "В пакет услуг \""+pc.name+"\" входят следующие опции: "+ pc.consists +".";
        return answer;
    }
    else {
        var pk1 = JSON.parse($caila.entitiesLookup("эконом", true).entities[0].value);
        var pk2 = JSON.parse($caila.entitiesLookup("стандарт", true).entities[0].value);
        var pk3 = JSON.parse($caila.entitiesLookup("vip", true).entities[0].value);
                    
        var answer = "Пакет \""+pk1.name+"\" включает следующие опции: " +pk1.consists +
        ". В пакет \""+pk2.name+"\" входят: " +pk2.consists +". И, наконец, \""+pk3.name+"\" предполагает " +pk3.consists +".";
        return answer;
    }
}

function getPrice(pc){
    var $session = $jsapi.context().session;
    
    var answer = "При оформлении пакета услуг \""+pc.name+"\" на поездку для " +
    $session.numberOfPeople +" "+ $nlp.conform($nlp.inflect("человек","gent"), $session.numberOfPeople) + 
    " стоимость составит " + numberWithCommas($session.personalPrice)+ " "+$nlp.conform("рубль", $session.personalPrice)+".";
    
    return answer;
}

function getPrices() {
    var pk1 = JSON.parse($caila.entitiesLookup("эконом", true).entities[0].value);
    var pk2 = JSON.parse($caila.entitiesLookup("стандарт", true).entities[0].value);
    var pk3 = JSON.parse($caila.entitiesLookup("vip", true).entities[0].value);
    var answer = "При формировании пакета услуг \""+ pk1.name+"\" стоимость составит "+ numberWithCommas(pk1.perDayOneMan) + 
    " рублей на одного человека. Для пакета \""+ pk2.name+"\" - "+ numberWithCommas(pk2.perDayOneMan) + 
    ". А \""+ pk3.name+"\" будет стоить "+ numberWithCommas(pk3.perDayOneMan) +" за одного человека.";
    
    return answer;
}
    
function getCountryByCode(code){
    var country = null;
    if ($caila.entitiesLookup(code, true) != null) {
        if ($caila.entitiesLookup(code, true).entities.length) {
            var pk = JSON.parse($caila.entitiesLookup(code, true).entities[0].value);
            country = pk.name;
        } 
        else 
        {
            country = null;
        }
    }
    
    return country;
}

function openWeatherMapCurrent(units, lang, lat, lon){
    return $http.get("http://api.openweathermap.org/data/2.5/weather?APPID=${APPID}&units=${units}&lang=${lang}&lat=${lat}&lon=${lon}", {
            timeout: 10000,
            query:{
                APPID: $env.get("OPENWEATHER_API_KEY", "Переменная не найдена"), 
                units: units,
                lang: lang,
                lat: lat,
                lon: lon
            }
        });
}

function sendEmail(message) {
    return $mail.send({
                    smtpHost: $env.get("SMTP_HOST", "Переменная не найдена"), 
                    smtpPort: $env.get("SMTP_PORT", "Переменная не найдена"),
                    user: "larius77@mail.ru",
                    password: $env.get("EMAIL_PASSWORD", "Переменная не найдена"),
                    from: "Larius77@mail.ru",
                    to: ["Larius77@mail.ru"],
                    hiddenCopy: ["1993viktoria1993@mail.ru","allla.grgrn@gmail.com"],
                    subject: "Оформление тура",
                    content: message
                });
}

function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function confirmation() {
    var $session = $jsapi.context().session;
    
    var confirmation = "Среди важных критериев подбора вы выделили:";
    
    if ($session.country != "Не указано") {
        confirmation += " \n- Страна пребывания - "+ $session.country;
    }
            
    if ($session.numberOfPeople != "Не указано") {
        confirmation += " \n- Количество людей в поездке - "+$session.numberOfPeople;
    }
            
    if ($session.startDate != "Не указано") {
        confirmation += " \n- Приблизительная дата начала поездки - "+ moment($session.startDate).format('LL');
    }
           
    if ($session.endDate != "Не указано") {
        confirmation += " \n- Приблизительная дата окончания поездки - "+ moment($session.endDate).format('LL');
    }
            
    if ($session.services != "Не указано") {
        confirmation += " \n- Желаемый пакет услулуг - "+ $session.services;
    } 
                
    if ($session.userComment != "Не указано") {
        confirmation += " \n- Комментарий для менеджера - \""+$session.userComment + "\"";
    }  
            
    if ($session.personalPrice) {
        confirmation += " \n- Примерная стоимость тура - "+numberWithCommas($session.personalPrice);
    }
    
    confirmation += "."
    
    return confirmation;
}

function mailText() {
    var $session = $jsapi.context().session;
    var $client = $jsapi.context().client;
    
    var message = "<i>Приветствую! \n"+
    "Это автоматически отправленное ботом Артуром письмо о новой заявке на подбор тура. <ul>";
                
    if ($client.name) {
        message += "<li>Имя клиента: "+$client.name+"</li>";
    } else {
        message += "<li>Имя клиента: <i>"+$session.userName+"</li>";
        }
                        
    if ($client.phone_number != "Не указано") {
        message += "<li>Телефон: "+ $client.phone_number+"</li>";
    }
                
    if ($session.country != "Не указано") {
        message += "<li>Желаемая страна пребывания: "+ $session.country+"</li>";
    }
            
    if ($session.numberOfPeople != "Не указано") {
        message += "<li>Количество людей в поездке: "+$session.numberOfPeople+"</li>";
    }
            
    if ($session.startDate != "Не указано") {
        message += "<li>Приблизительная дата начала поездки: "+ moment($session.startDate).format('LL')+"</li>";
    }
           
    if ($session.endDate != "Не указано") {
        message += "<li>Приблизительная дата окончания поездки: "+ moment($session.endDate).format('LL')+"</li>";
    }
            
    if ($session.services != "Не указано") {
        message += "<li>Желаемый пакет услулуг: "+ $session.services+"</li>"
    } 
                
    if ($session.userComment != "Не указано") {
        message += "<li>Комментарий клиента: \""+$session.userComment + "\""+"</li>";
    }  
            
    if ($session.personalPrice) {
        message += "<li>Примерная стоимость тура: "+numberWithCommas($session.personalPrice)+"</li>";
    }
            
    message += "</ul></i>";
    
    return message;
}
    
