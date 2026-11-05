function getNumbers(value) {
    if (value.trim() === "") {
        return [];
    }

    var parts = value.split(/\s+/);
    var numbers = [];

    for (var i = 0; i < parts.length; i++) {
        var item = parts[i].trim();
        var number = Number(item);

        if (item === "" || isNaN(number)) {
            return null;
        }

        numbers.push(number);
    }

    return numbers;
}

function insertionSort(array) {
    var result = array.slice();

    for (var i = 1; i < result.length; i++) {
        var current = result[i];
        var j = i - 1;

        while (j >= 0 && result[j] > current) {
            result[j + 1] = result[j];
            j--;
        }

        result[j + 1] = current;
    }

    return result;
}

function showError(message) {
    var result = document.getElementById("result");
    result.className = "error";
    result.textContent = message;
}

function sortArray(event) {
    event.preventDefault();

    var input = document.getElementById("arrayInput");
    var numbers = getNumbers(input.value);

    if (numbers === null) {
        showError("Ошибка: введите только числа через пробел.");
        return;
    }

    if (numbers.length === 0) {
        showError("Поле пустое. Введите хотя бы одно число.");
        return;
    }

    var sorted = insertionSort(numbers);
    var result = document.getElementById("result");

    result.className = "success";
    result.innerHTML = "Исходный массив: " + numbers.join(", ") +
        "<br>Отсортированный массив: " + sorted.join(", ");
}

window.onload = function () {
    document.getElementById("sortForm").addEventListener("submit", sortArray);
};
