`<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>ShortyLink</title>
    <style>
        /* Основные стили */
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 0;
        }

        h2 {
            text-align: center;
            padding: 20px 0;
            font-family: 'Roboto', sans-serif;
            font-size: 24px;
            color: #333;
        }

        /* Форма ввода */
        #inputForm {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
            background: linear-gradient(to top, #ffffff, #fff);
            width: auto; /* Добавьте эту строку */
            height: 170px;
            margin: 0 auto;
        }

        /* Лейблы */
        label {
            font-family: 'Roboto', sans-serif;
            font-size: 16px;
            color: #333;
            margin-bottom: 5px;
        }

        /* Ввод значения */
        input[type="text"] {
            padding: 10px;
            font-size: 16px;
            border-radius: 5px;
            border: 1px solid #ccc;
            width: 200px;
            margin-bottom: 10px;
            text-align: center;
        }

        /* Кнопка отправки */
        input[type="submit"] {
            padding: 10px 20px;
            font-size: 16px;
            border-radius: 5px;
            border: none;
            background-color: #4CAF50;
            color: #fff;
            cursor: pointer;
        }

        /* Кнопка отправки (hover) */
        input[type="submit"]:hover {
            background-color: #45a049;
        }

        /* Результат */
        #result {
            text-align: center;
            padding: 10px 0;
            font-family: 'Roboto', sans-serif;
            font-size: 18px;
            color: #333;
            word-wrap: break-word;
            max-width: 300px;
            margin: 0 auto;
            width: auto; /* Добавьте эту строку */
            white-space: nowrap; /* Добавьте эту строку */
        }

        /* Введенное значение */
        #enteredValue {
            text-align: center;
            padding: 28px 0;
            font-family: 'Roboto', sans-serif;
            font-size: 16px;
            color: #333;
            width: auto; /* Добавьте эту строку */
            margin: 0 auto;
        }

        #loading {
            display: none;
            margin-top: 10px;
            text-align: center;
            width: 300px;
            margin: 0 auto;
        }

        /* Disabled */
        .disabled {
            opacity: 0.5;
            cursor: not-allowed;
            background-color: #ccc;
            color: #666;
        }

        #login-result {
            font-size: 7px;
            text-align: center;
            margin: auto;
            margin-top: 8px;
            width: 300px;
        }

        #loading {
            font-size: 11px;
        }

        #loading-result {
            text-align: center;
            margin: auto;
            margin-top: 12px;
            width: 300px;
        }

        .loading-dots {
            display: inline-block;
            width: 50px;
            height: 10px;
            font-size: 15px;
            line-height: 10px;
            vertical-align: middle;
            margin-top: 12px;
            margin-bottom: 10px;
        }

        .loading-dots::before {
            content: "• • •";
            animation: loading-dots 2s linear infinite;
        }

        @keyframes loading-dots {
            0% {
                content: "# • •";
            }
            20% {
                content: "# • •";
            }
            30% {
                content: "• # •";
            }
            50% {
                content: "• # •";
            }
            60% {
                content: "• • #";
            }
            80% {
                content: "• • #";
            }
            90% {
                content: "# • •";
            }
            100% {
                content: "# • •";
            }
        }


    </style>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
</head>
<body>
<h2>The input link</h2>
<form id="inputForm">
    <label for="value">Enter the link:</label>
    <input type="text" id="value" name="value">
    <input type="submit" value="Get short link">
    <p id="enteredValue"></p>
</form>
<div id="result" style="min-height: 30px;"></div>

<div id="loading-result" style="display: none;">
    <div class="loading-dots"></div>
    <div id="loading" style="display: block;"> - waiting - </div>
</div>

<div >
    <hr style="margin: 6px 0;">
</div>

<h2 style="padding: 0px 0; margin-bottom: 0px;  margin-top: 0px;">.......</h2>
<form id="loginForm" style="text-align: center; margin-top: -20px;">

</form>

<!--<div id="login-result" style="display: none;"></div>-->

<script>

    async function updateUrlList(pageNumber = 1) {
        const response = await fetch(`/home/controller/list?pageNumber=${pageNumber}`);
        const data = await response.json();

        const urls = JSON.parse(data.message);
        const totalPages = data.totalPages;

        if (urls) {
            const urlList = document.querySelector('ul');
            urlList.innerHTML = '';

            Object.keys(urls).forEach(key => {
                const urlListItem = document.createElement('li');
                urlListItem.innerText = `${key} ---> ${urls[key]}`;
                urlList.appendChild(urlListItem);
            });

            const loginH2Element = document.querySelector('h2');
            loginH2Element.innerText = `welcome  ${data.result}`;

            let paginationContainer = document.querySelector('div.pagination');

            if (paginationContainer) {
                paginationContainer.innerHTML = '';
            } else {
                paginationContainer = document.createElement('div');
                paginationContainer.classList.add('pagination');
                paginationContainer.style.textAlign = 'center';
                paginationContainer.style.margin = '0 auto';
                loginH2Element.parentNode.appendChild(urlList);
                loginH2Element.parentNode.appendChild(paginationContainer);
            }

            // Удалить существующую пагинацию, если она есть
            const oldPagination = document.querySelector('#pagination-container');
            if (oldPagination) {
                oldPagination.remove();
            }

            for (let i = 1; i <= totalPages; i++) {
                const pageNumberButton = document.createElement('button');
                pageNumberButton.textContent = i;
                pageNumberButton.style.margin = '5px';
                pageNumberButton.style.border = '1px solid #ccc';
                pageNumberButton.style.borderRadius = '5px';
                pageNumberButton.style.padding = '5px';
                pageNumberButton.style.cursor = 'pointer';

                if (i === pageNumber) {
                    pageNumberButton.style.background = 'lightgray';
                } else {
                    pageNumberButton.style.background = 'none';
                }

                pageNumberButton.onclick = async () => {
                    await updateUrlList(i);
                };

                paginationContainer.appendChild(pageNumberButton);
            }
        }
    }

    document.getElementById('inputForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const value = document.getElementById('value').value;
        document.getElementById('enteredValue').innerText = `Your link:\n${value}`;
        document.getElementById('loading').style.display = 'block';
        document.getElementById('inputForm').style.pointerEvents = 'none';
        const inputField = document.getElementById('value');
        const submitButton = document.getElementById('inputForm').querySelector('input[type="submit"]');
        inputField.classList.add('disabled');
        submitButton.classList.add('disabled');
        document.getElementById('value').value = ''; // очистить поле ввода
        document.getElementById('value').blur(); // убрать фокус с поля ввода
        document.getElementById('result').style.display = 'none';
        document.getElementById('loading-result').style.display = 'block';
        const response = await fetch(`/home/controller?value=${value}`);

        document.getElementById('loading').style.display = 'none';
        document.getElementById('inputForm').style.pointerEvents = 'auto';
        inputField.classList.remove('disabled');
        submitButton.classList.remove('disabled');
        document.getElementById('loading-result').style.display = 'none';
        document.getElementById('result').style.display = 'block';



        if (response.ok) {
            const data = await response.json();
            document.getElementById('result').innerText = `Shorted link:\n${data.huy}`;
        } else {
            document.getElementById('result').innerText = 'error';
        }

        await updateUrlList();
    });

    document.addEventListener('DOMContentLoaded', async () => {
        //...

        const loginH2Element = document.querySelectorAll('h2')[1];
        const response = await fetch('/home/controller/list');
        const data = await response.json();

        loginH2Element.innerText = `welcome  ${data.result}`;

        const urls = JSON.parse(data.message);

        if (urls) {
            const urlList = document.createElement('ul');

            Object.keys(urls).forEach(key => {
                const urlListItem = document.createElement('li');
                urlListItem.innerText = `${key} ---> ${urls[key]}`;
                urlList.appendChild(urlListItem);
            });

            // Добавляем список к элементу h2
            loginH2Element.parentNode.appendChild(urlList);
            urlList.style.margin = "0 auto"; // выравниваем по центру горизонтально
            urlList.style.padding = "0"; // сбрасываем отступы
            urlList.style.listStyle = "none"; // сбрасываем маркеры списка
            urlList.style.textAlign = "center"; // выравниваем текст по центру


            // Добавляем пагинацию
            //const pagination = document.createElement('div');
            //pagination.style.textAlign = 'center';
            await updateUrlList();

            // Добавляем кнопку "exit"
            const exitButton = document.createElement('button');
            exitButton.textContent = 'Exit';
            exitButton.style.marginLeft = 'auto';
            exitButton.style.marginRight = 'auto';
            exitButton.style.marginTop = '0px'; // добавляем отступ сверху
            exitButton.style.display = 'block';

            // Добавляем собыие на кнопку "exit"
            exitButton.onclick = async () => {
                const response = await fetch('/home/controller/login', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: 'action=logout' });
                if (response.ok) {
                    // Обновляем страницу после выхода из системы
                    location.reload();
                } else {
                    console.error('Ошибка при выходе из системы');
                }
            };

            // Добавляем кнопку "exit" к элементу h2
            loginH2Element.parentNode.appendChild(exitButton);

            // Добавляем кнопку удаления аккаунта
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Удалить аккаунт';
            deleteButton.style.marginLeft = 'auto';
            deleteButton.style.marginRight = 'auto';
            deleteButton.style.marginTop = '18px'; // добавляем отступ сверху
            deleteButton.style.display = 'block';

            // Добавляем событие на кнопку удаления аккаунта
            deleteButton.onclick = async () => {
                const response = await fetch('/home/controller/delete', { method: 'DELETE' });
                if (response.ok) {
                    // Обновляем страницу после удаления аккаунта
                    location.reload();
                } else {
                    console.error('Ошибка при удалении аккаунта');
                }
            };

            // Добавляем кнопку удаления аккаунта к элементу h2
            loginH2Element.parentNode.appendChild(deleteButton);
        }
    });


</script>
</body>
</html>`