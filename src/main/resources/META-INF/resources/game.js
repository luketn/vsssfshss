function initKeyHandler(gameElement) {
    let x = 0;
    let y = 0;
    let speed = 5;

    document.addEventListener('keydown', (event)=>{
        let fishPlayer = document.getElementById("fishplayer");
        switch (event.key) {
            case "a":{
                x-=speed;
                break;
            }
            case "d":{
                x+=speed;
                break;
            }
            case "s":{
                y+=speed;
                break;
            }
            case "w":{
                y-=speed;
                break;
            }
        }
        fishPlayer.setAttribute('transform', `translate(${x},${y})`);
    })
}

function playGame() {
    var gameElement = document.getElementById('game');
    gameElement.classList.remove('hidden');

    initKeyHandler(gameElement);
}


