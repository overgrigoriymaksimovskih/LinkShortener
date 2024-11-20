<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Моя страница</title>
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
            box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.1);
            background: linear-gradient(to bottom, #f0f0f0, #fff);
            width: 300px;
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
            padding: 15px 0;
            font-family: 'Roboto', sans-serif;
            font-size: 18px;
            color: #333;
            width: 300px;
            margin: 0 auto;
        }

        /* Введенное значение */
        #enteredValue {
            text-align: center;
            padding: 38px 0;
            font-family: 'Roboto', sans-serif;
            font-size: 16px;
            color: #333;
            width: 300px;
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
        #loading {
            font-size: 11px;
        }

        #loading-result {
            text-align: center;
            margin: auto;
            margin-top: 8px;
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
<h2>Форма ввода</h2>
<form id="inputForm">
    <label for="value">Значение:</label>
    <input type="text" id="value" name="value">
    <input type="submit" value="Отправить">
    <p id="enteredValue"></p>
</form>
<div id="result"></div>


<div id="loading-result" style="display: none;">
    <div class="loading-dots"></div>
    <div id="loading" style="display: block;"> - ожидаем - </div>
</div>

<script>
    document.getElementById('inputForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const value = document.getElementById('value').value;
        document.getElementById('enteredValue').innerText = `Введенное значение: ${value}`;
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
        const response = await fetch(`http://localhost:8080/home/controller?value=${value}`);

        document.getElementById('loading').style.display = 'none';
        document.getElementById('inputForm').style.pointerEvents = 'auto';
        inputField.classList.remove('disabled');
        submitButton.classList.remove('disabled');
        document.getElementById('loading-result').style.display = 'none';
        document.getElementById('result').style.display = 'block';

        if (response.ok) {
            const data = await response.json();
            document.getElementById('result').innerText = `${data.huy}`;
        } else {
            document.getElementById('result').innerText = 'error';
        }
    });
</script>
</body>
</html>