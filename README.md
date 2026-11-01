# Nebula Visuals (Fabric, Minecraft 1.21.11)

Клиентский мод: меню на Right Shift, свой экран входа, настраиваемый HUD, keystrokes, инфо о цели,
цифры урона, маркер попадания, fullbright, режим максимального FPS.

## Как собрать без установки программ (GitHub Actions)
1. Зарегистрируйтесь на github.com и создайте пустой репозиторий.
2. Add file -> Upload files: загрузите ВСЕ файлы проекта, включая папку `.github`.
   (Если браузер не даёт перетащить скрытую папку, создайте файл `.github/workflows/build.yml`
   вручную через Add file -> Create new file и вставьте туда содержимое из архива.)
3. Откройте вкладку Actions -> Build Nebula Visuals -> дождитесь зелёной галочки.
4. Внутри запуска, раздел Artifacts: скачайте `NebulaVisuals-jar`, внутри `nebula-visuals-1.0.0.jar`.
5. Положите jar и Fabric API в папку `mods` вместе с Fabric Loader для 1.21.11.

## Управление
- Right Shift: меню (во время игры), повторно Right Shift закрывает.
- Меню -> HUD -> «Перемещать элементы»: перетаскивание мышью.
- Слова для фильтра чата: `config/nebula_visuals.json`, поле `bannedWords`.

## Для максимального FPS
Режим макс. FPS в меню меняет настройки игры. Для большего прироста поставьте рядом Sodium, Lithium,
FerriteCore (ищите версии для 1.21.11 на modrinth.com).
