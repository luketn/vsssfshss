/**
 * Speaks the given text using a screen reader.
 * 
 * @param text the text to speak
 */
function speak(text) {
    var alertBox = document.getElementById('accessibleAlert');
    alertBox.innerText = text;
    window.setTimeout(() => {
        alertBox.innerText = '';
    }, 1000);
}
