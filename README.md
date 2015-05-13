# UDP-Android-Client
Android-клиент для системы учета рекламаций по Парус-8 (УДП)

Сразу после первого открытия приложения Вы попадете на на экран настроек, где необходимо ввести логин и пароль для доступа в УДП. Надеюсь все помнят свои учетные данные biggrin.gif . Для тех у кого ее нет, получаем учетную запись в штатном порядке, либо используем гостевой вход (`guest/guest`). После ввода учетных данных нажимаем иконку возврата на главную страницу приложения.

![alt tag](http://pmo.parus.ua/pics/apk_settings.jpg)

Также при первом входе производится считывание списка приложений и разделов Парус-8, для последующего кеширования. Это происходит один раз при первом входе в приложение, поэтому первый раз может понадобиться немного подождать окончания этого процесса  (в случае необходимости всегда можно принудительно обновить кеш, выбрав соответствующий пункт на экране настроек).

![alt tag](http://pmo.parus.ua/pics/apk_wait.jpg)

Главный экран приложения представляет собой список рекламаций, отсортированный по дате последнего действия над ними. Сразу после входа вы уведите список рекламаций, в которых вы являетесь исполнителем.

![alt tag](http://pmo.parus.ua/pics/apk_main.jpg)

На главном экране доступны действия:
- ![alt tag](http://pmo.parus.ua/pics/apk_search.jpg) "Отбор" - выбор сохраненных или задание новых условий отбора;
- ![alt tag](http://pmo.parus.ua/pics/apk_add.jpg) "Добавление" - добавление новой рекламации;
- ![alt tag](http://pmo.parus.ua/pics/apk_refresh.jpg) "Обновление" - обновление списка в соответствие с текущими условиями отбора.
- Нажатие на любую рекламацию откроет экран просмотра её детальной информации.

#### Немного про условия отбора…

Выбирая на главном экране действие "Отбор", Вы попадаете на экран со списком сохраненных условий отбора (запросов). 

![alt tag](http://pmo.parus.ua/pics/apk_filters.jpg)

Список содержит, как уже преднастроенные запросы, так и сохраненные Вами. Вам доступно:
- ![alt tag](http://pmo.parus.ua/pics/apk_add.jpg) добавление нового запроса;
- ![alt tag](http://pmo.parus.ua/pics/apk_edit.jpg) редактирование существующего
- ну и собственно выполнение любого запроса из списка (простым нажатием на название запроса). 

*Заметьте, что Вы не можете редактировать преднастроенные запросы.*

На экране добавления/редактирования запроса вы можете:
 - ![alt tag](http://pmo.parus.ua/pics/apk_save.jpg) сохранить и выполнить запрос 
 - ![alt tag](http://pmo.parus.ua/pics/apk_do.jpg) выполнить запрос без сохранения
 - ![alt tag](http://pmo.parus.ua/pics/apk_clear.jpg) очистить условия отбора

На экране просмотра рекламации - вы можете (в зависимости от ваших прав и состояния рекламации) выполнять следующие привычные для Вас действия:
 - ![alt tag](http://pmo.parus.ua/pics/apk_forward.jpg) перевести рекламацию в следующее состояние;
 - ![alt tag](http://pmo.parus.ua/pics/apk_return.jpg) вернуть рекламацию в предыдущее состояние;
 - ![alt tag](http://pmo.parus.ua/pics/apk_note.jpg) прокомментировать рекламацию;
 - ![alt tag](http://pmo.parus.ua/pics/apk_adddoc.jpg) присоединить к рекламации файл;
 - ![alt tag](http://pmo.parus.ua/pics/apk_drow.jpg) под этой кнопкой, скрываются более редкие действия, такие как (`Исправить`, `Аннулировать`, `Перенаправить другому исполнителю`, `Удалить`).

Если к рекламации присоединены файлы (их список находится в самом конце истории), их можно скачать нажатием на иконку ![alt tag](http://pmo.parus.ua/pics/apk_download.jpg)
