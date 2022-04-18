function initKeyHandler(gameElement) {
    document.addEventListener('keydown', (event)=>{
        console.log('Keydown', event);
        switch (event.key) {
            case "a":{
                console.log('go left!');
                break;
            }
            case "d":{
                console.log('go right!');
                break;
            }
            case "s":{
                console.log('go down!');
                break;
            }
            case "w":{
                console.log('go up!');
                break;
            }
        }
    })
}

function playGame() {
    var gameElement = document.getElementById('game');
    gameElement.classList.remove('hidden');

    initKeyHandler(gameElement);
}


