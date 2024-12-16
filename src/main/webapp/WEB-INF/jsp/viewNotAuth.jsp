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

<h2 style="padding: 0px 0; margin-bottom: 10px;  margin-top: 10px;">Login</h2>
<form id="loginForm" style="text-align: center; margin-top: 4px;">
    <input type="text" id="login" name="login" placeholder="Login" style="width: 150px; padding: 5px; font-size: 14px; border-radius: 5px; border: 1px solid #ccc; text-align: center;">
    <br style="height: 5px;">
    <input type="password" id="password" name="password" placeholder="Password" style="width: 150px; padding: 5px; font-size: 14px; border-radius: 5px; border: 1px solid #ccc; text-align: center;">
    <br style="height: 300px;">
    <input type="submit" id="registerButton" value="Register" style="padding: 5px 10px; font-size: 14px; border-radius: 5px; border: none; background-color: #4CAF50; color: #fff; cursor: pointer; margin-top: 10px; margin-right: 10px;">
    <input type="submit" id="loginButton" value="Login" style="padding: 5px 10px; font-size: 14px; border-radius: 5px; border: none; background-color: #4CAF50; color: #fff; cursor: pointer; margin-top: 10px;">
</form>

<div id="login-result" style="display: none;"></div>

<script>
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
    });

    document.getElementById('loginForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const login = document.getElementById('login').value;
        const password = document.getElementById('password').value;
        document.getElementById('login-result').style.display = 'block';
        document.getElementById('loginForm').style.pointerEvents = 'none';
        const inputField = document.getElementById('login');
        const submitButton = document.getElementById('loginForm').querySelector('input[type="submit"]');
        inputField.classList.add('disabled');
        submitButton.classList.add('disabled');
        document.getElementById('login-result').innerText = 'L.O.A.D.I.N.G';

        const response = await fetch('/home/controller/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `login=${login}&password=${password}`
        });

        document.getElementById('login-result').style.display = 'block';
        document.getElementById('loginForm').style.pointerEvents = 'auto';
        inputField.classList.remove('disabled');
        submitButton.classList.remove('disabled');

        if (response.ok) {
            const data = await response.json();
            if (data.result === "success") {
                // Обновляем страницу после авторизации
                location.reload();
            } else {
                document.getElementById('login-result').innerText = data.message;
            }
        } else {
            document.getElementById('login-result').innerText = 'error 57';
        }
    });

    document.getElementById('registerButton').addEventListener('click', async (event) => {
        event.preventDefault();
        const login = document.getElementById('login').value;
        const password = document.getElementById('password').value;
        document.getElementById('login-result').style.display = 'block';
        document.getElementById('loginForm').style.pointerEvents = 'none';
        const inputField = document.getElementById('login');
        const registerButton = document.getElementById('registerButton');
        const loginButton = document.getElementById('loginButton');
        inputField.classList.add('disabled');
        registerButton.classList.add('disabled');
        loginButton.classList.add('disabled');
        document.getElementById('login-result').innerText = 'L.O.A.D.I.N.G';

        const response = await fetch('/home/controller/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `login=${login}&password=${password}`
        });

        if (response.ok) {
            const data = await response.json();
            if (data.result === "success") {
                // Авторизуемся автоматически
                const authResponse = await fetch('/home/controller/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: `login=${login}&password=${password}`
                });

                if (authResponse.ok) {
                    const authData = await authResponse.json();
                    if (authData.result === "success") {
                        // Обновляем страницу после авторизации
                        location.reload();
                    } else {
                        document.getElementById('login-result').innerText = authData.message;
                    }
                } else {
                    document.getElementById('login-result').innerText = 'error';
                }
            } else {
                document.getElementById('login-result').style.display = 'block';
                document.getElementById('loginForm').style.pointerEvents = 'auto';
                inputField.classList.remove('disabled');
                registerButton.classList.remove('disabled');
                loginButton.classList.remove('disabled');
                document.getElementById('login-result').innerText = data.message;
            }
        } else {
            document.getElementById('login-result').style.display = 'block';
            document.getElementById('loginForm').style.pointerEvents = 'auto';
            inputField.classList.remove('disabled');
            registerButton.classList.remove('disabled');
            loginButton.classList.remove('disabled');
            document.getElementById('login-result').innerText = 'error';
        }
    });
</script>
</body>
</html>`