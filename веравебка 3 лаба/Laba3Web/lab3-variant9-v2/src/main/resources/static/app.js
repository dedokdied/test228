var TOKEN_KEY = "store_token";
var USER_KEY = "store_user";

function el(tag, className, text) {
    var node = document.createElement(tag);
    if (className) {
        node.className = className;
    }
    if (text !== undefined) {
        node.textContent = text;
    }
    return node;
}

function showMessage(text, isError) {
    var box = document.getElementById("message");
    if (!box) {
        return;
    }
    box.textContent = text;
    box.className = isError ? "error" : (text ? "success" : "");
}

function getToken() {
    try {
        return localStorage.getItem(TOKEN_KEY);
    } catch (e) {
        return null;
    }
}

function getStoredUser() {
    try {
        var raw = localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch (e) {
        return null;
    }
}

function isAuthenticated() {
    return !!getToken();
}

function isAdmin() {
    var user = getStoredUser();
    return !!user && user.role === "ADMIN";
}

function saveSession(data) {
    localStorage.setItem(TOKEN_KEY, data.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify({
        username: data.username,
        role: data.role
    }));
}

function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

function redirectToLogin() {
    var current = window.location.pathname.split("/").pop() || "index.html";
    if (window.location.search) {
        current += window.location.search;
    }
    if (current.indexOf("login.html") === 0) {
        return;
    }
    window.location.href = "login.html?next=" + encodeURIComponent(current);
}

function updateAuthUi() {
    var user = getStoredUser();
    var link = document.getElementById("authLink");

    if (link) {
        if (user) {
            link.textContent = "Выйти (" + user.username + ")";
            link.href = "#";
            link.onclick = function (event) {
                event.preventDefault();
                clearSession();
                window.location.href = "index.html";
            };
        } else {
            link.textContent = "Войти";
            link.href = "login.html";
            link.onclick = null;
        }
    }

    var adminElements = document.querySelectorAll(".admin-only");
    for (var i = 0; i < adminElements.length; i++) {
        adminElements[i].style.display = isAdmin() ? "" : "none";
    }
}

function apiRequest(path, options) {
    var opts = Object.assign({}, options || {});
    var headers = Object.assign({ "Content-Type": "application/json" }, opts.headers || {});
    var token = getToken();
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }
    opts.headers = headers;

    return fetch(path, opts).then(function (response) {
        if (response.status === 204) {
            return null;
        }
        return response.json().catch(function () {
            return null;
        }).then(function (data) {
            if (!response.ok) {
                if (response.status === 401 && path.indexOf("/api/auth/login") === -1) {
                    clearSession();
                    redirectToLogin();
                }
                var message = (data && data.error) || "Ошибка запроса";
                throw new Error(message);
            }
            return data;
        });
    });
}

function getProducts() {
    return apiRequest("/api/products");
}

function getProductById(id) {
    return apiRequest("/api/products/" + id);
}

function createProduct(product) {
    return apiRequest("/api/products", {
        method: "POST",
        body: JSON.stringify(product)
    });
}

function updateProduct(id, product) {
    return apiRequest("/api/products/" + id, {
        method: "PUT",
        body: JSON.stringify(product)
    });
}

function deleteProduct(id) {
    return apiRequest("/api/products/" + id, {
        method: "DELETE"
    });
}

function buyProduct(id, quantity) {
    return apiRequest("/api/products/" + id + "/purchase", {
        method: "POST",
        body: JSON.stringify({ quantity: quantity })
    });
}

function login(username, password) {
    return apiRequest("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username: username, password: password })
    });
}

function productInfo(item) {
    return "Цена: " + item.price + " | На складе: " + item.countInStock + " шт.";
}

function loadProductList(container) {
    getProducts().then(function (products) {
        container.textContent = "";

        if (products.length === 0) {
            container.appendChild(el("p", "", "Товаров пока нет. Добавьте первый на странице «Добавить»."));
            updateAuthUi();
            return;
        }

        products.forEach(function (item) {
            var article = el("article", "product-item");
            article.appendChild(el("h2", "", item.id + ". " + item.name));
            article.appendChild(el("p", "brand", item.brand));
            article.appendChild(el("p", "", productInfo(item)));

            var link = el("a", "admin-only", "Изменить");
            link.href = "edit.html?id=" + item.id;
            article.appendChild(link);

            container.appendChild(article);
        });

        updateAuthUi();
    }).catch(function (err) {
        container.textContent = "";
        container.appendChild(el("p", "error", err.message));
    });
}

function loadDeleteList(container) {
    getProducts().then(function (products) {
        container.textContent = "";

        if (products.length === 0) {
            container.appendChild(el("p", "", "Товаров нет — удалять нечего."));
            return;
        }

        var list = el("ul", "delete-list");

        products.forEach(function (item) {
            var row = el("li");
            var label = el("span");
            label.appendChild(el("strong", "", item.name));
            label.appendChild(document.createTextNode(" — " + item.brand + " (" + productInfo(item) + ")"));
            row.appendChild(label);

            var button = el("button", "", "Удалить");
            button.addEventListener("click", function () {
                button.disabled = true;
                deleteProduct(item.id).then(function () {
                    row.remove();
                    showMessage("Товар удалён.", false);
                }).catch(function (err) {
                    button.disabled = false;
                    showMessage(err.message, true);
                });
            });
            row.appendChild(button);

            list.appendChild(row);
        });

        container.appendChild(list);
    }).catch(function (err) {
        container.textContent = "";
        container.appendChild(el("p", "error", err.message));
    });
}

function initAddForm(form) {
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        showMessage("", false);

        createProduct({
            name: document.getElementById("name").value,
            brand: document.getElementById("brand").value,
            price: parseFloat(document.getElementById("price").value),
            countInStock: parseInt(document.getElementById("countInStock").value, 10)
        }).then(function () {
            window.location.href = "index.html";
        }).catch(function (err) {
            showMessage(err.message, true);
        });
    });
}

function initEditForm(form) {
    var id = new URLSearchParams(window.location.search).get("id");
    var nameInput = document.getElementById("name");
    var brandInput = document.getElementById("brand");
    var priceInput = document.getElementById("price");
    var stockInput = document.getElementById("countInStock");
    var submitButton = form.querySelector("button[type='submit']");

    if (!id) {
        showMessage("Не указан id товара в адресе страницы.", true);
        submitButton.disabled = true;
        return;
    }

    getProductById(id).then(function (item) {
        nameInput.value = item.name;
        brandInput.value = item.brand;
        priceInput.value = item.price;
        stockInput.value = item.countInStock;
    }).catch(function (err) {
        showMessage(err.message, true);
        submitButton.disabled = true;
        return;
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        showMessage("", false);

        updateProduct(id, {
            name: nameInput.value,
            brand: brandInput.value,
            price: parseFloat(priceInput.value),
            countInStock: parseInt(stockInput.value, 10)
        }).then(function () {
            window.location.href = "index.html";
        }).catch(function (err) {
            showMessage(err.message, true);
        });
    });
}

function refreshProductSelect() {
    var select = document.getElementById("productSelect");

    return getProducts().then(function (products) {
        select.textContent = "";

        if (products.length === 0) {
            var emptyOption = document.createElement("option");
            emptyOption.value = "";
            emptyOption.textContent = "Нет товаров — добавьте на странице «Добавить»";
            select.appendChild(emptyOption);
            return;
        }

        products.forEach(function (item) {
            var option = document.createElement("option");
            option.value = item.id;
            option.textContent = item.id + " — " + item.name + " (" + item.brand + "), в наличии: " + item.countInStock + " шт.";
            select.appendChild(option);
        });
    });
}

function initBuyPage(form) {
    var select = document.getElementById("productSelect");
    var quantityInput = document.getElementById("quantity");

    refreshProductSelect().catch(function (err) {
        showMessage(err.message, true);
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        showMessage("", false);

        if (!isAuthenticated()) {
            redirectToLogin();
            return;
        }

        if (!select.value) {
            showMessage("Сначала добавьте товар на странице «Добавить».", true);
            return;
        }

        var quantity = parseInt(quantityInput.value, 10);
        if (isNaN(quantity) || quantity <= 0) {
            showMessage("Количество должно быть целым числом больше нуля.", true);
            return;
        }

        buyProduct(select.value, quantity).then(function (product) {
            showMessage("Покупка оформлена: " + product.name + ". Осталось на складе " + product.countInStock + " шт.", false);
            return refreshProductSelect();
        }).catch(function (err) {
            showMessage(err.message, true);
        });
    });
}

function initLoginForm(form) {
    var user = getStoredUser();
    if (user) {
        showMessage("Вы уже вошли как " + user.username + " (" + user.role + ").", false);
    }

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        showMessage("", false);

        login(
            document.getElementById("username").value.trim(),
            document.getElementById("password").value
        ).then(function (data) {
            saveSession(data);
            var next = new URLSearchParams(window.location.search).get("next") || "index.html";
            window.location.href = next;
        }).catch(function (err) {
            showMessage(err.message, true);
        });
    });
}

window.onload = function () {
    updateAuthUi();

    var productList = document.getElementById("productList");
    var deleteList = document.getElementById("deleteList");
    var addForm = document.getElementById("addForm");
    var editForm = document.getElementById("editForm");
    var buyForm = document.getElementById("buyForm");
    var loginForm = document.getElementById("loginForm");

    if (productList) {
        loadProductList(productList);
    }
    if (deleteList) {
        loadDeleteList(deleteList);
    }
    if (addForm) {
        initAddForm(addForm);
    }
    if (editForm) {
        initEditForm(editForm);
    }
    if (buyForm) {
        initBuyPage(buyForm);
    }
    if (loginForm) {
        initLoginForm(loginForm);
    }
};
