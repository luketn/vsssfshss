const renderers = [
    [/(https?:\/\/[^\s]+)/g, renderLink],
]

function renderLink(url) {
    return `<a href="${url}" target="_blank">${url}</a>`
}

function escapeHTML(unsafeText) {
    return unsafeText.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function generateHTMLForMessage(chatMessage) {
    return `
        <div class="message">
            <div class="message__author">${escapeHTML(chatMessage.name)}:</div>
            <div class="message__content">
                ${renderMessage(escapeHTML(chatMessage.message))}
            </div>
        </div>
    `
}

function renderMessage(message) {
    message = message.replace('\n', '<br>');
    for (const [regex, renderer] of renderers) {
        message = message.replace(regex, renderer)
    }
    return message
}
