## Описание ##
Некоторая фирма "N" предоставляет услуги по поставке техники и ПО к ней и не только. В основном на языке программирования Axapta. И как и везде всегда могут быть браки, баги и т.п. И на такой случай есть мое приложение, в котором создается заявка на исправление той или иной ошибки. К заявке можно прикрепить файл, описать ошибку и указать всю нужную информацию. Далее эта заявка высвечивается у админа в таблице, и он может обработать ее, прокомментировать и т.д.(Также есть админская и пользовательская версия приложения). И все изменения отображаются в режиме реального времени, т.к. все завязано на БД. Админ также может фильтровать и искать все заявки, чтобы было удобно работать. Также можно добавлять, удалять, изменять заявки. Еще админ может добавлять новых пользователей и смотреть информацию о существующих.

## Start ##
1. sudo apt-get update
2. git clone https://github.com/atommaks/practice-2020-Applicant
3. Установка mysql-сервера 8 версии (нужна 8 версия или выше для корректной работы):
    * sudo apt-get install wget
    * wget https://dev.mysql.com/get/mysql-apt-config_0.8.15-1_all.deb
    * sudo dpkg -i mysql-apt-config_0.8.15-1_all.deb
    *  В 1-ом пункте в открывшимся листе выберете 8 версию
    * Нажмите ОК
    * sudo apt update
    * sudo apt install mysql-server
    * установите для пользователя root пароль "atom1105"
    * далее везде со всем согласитесь
    * С помощью команды apt policy mysql-server проверьте установленную версию
4. Запуск mysql-сервера:
    * sudo systemctl enable —now mysql
    * C помощью команды systemctl status mysql убедитесь, что сервер запущен
5. Настройка mysql-сервера:
    * sudo mysql -u root -p
    * Введите пароль (он должен быть atom1105)
    * create database applicant;
    * exit;
    * sudo mysql -u root -p applicant < ~/practice-2020-Applicant/database/database.sql
    * sudo mysql
    * UNINSTALL PLUGIN validate-password;
    * ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'atom1105';
    * exit;
6. Если не установлена Java 8 или выше версии, то установите ее, следуя [инструкции](https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-on-ubuntu-18-04-ru) 
7. Запуск приложения:
    * cd practice-2020-Applicant/out/artifacts/Applicant_jar/
    * java -jar Applicant.jar
    
    ## Информация о пользователях ##
1. Логин: Горовец Максим    Пароль: 123 (Имеет администраторские права)
2. Логин: Вася Пупкин   Пароль: 789    (Не имеет администраторских прав)

## Команда ##
1. Горовец Максим Владиславович ИУ9-43Б  ([@atommaks](https://github.com/atommaks))
