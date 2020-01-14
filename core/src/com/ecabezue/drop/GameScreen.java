package com.ecabezue.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    public SpriteBatch batch;
    private Rectangle bucket;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private BitmapFont puntuacio;
    private int contador;
    private float speed = 3;
    final Drop game;
    public BitmapFont introText;

    public GameScreen(final Drop game) {
        this.game = game;
        // Carreguem les imatges del cub i de la gota
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        puntuacio = new BitmapFont();


        // Carreguem el só
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // Fem que es repeteixi cada cop que acabi el só de fons de la pluja
        // I fem que soni
        rainMusic.setLooping(true);
        rainMusic.play();

        // Inicialitzem la camera amb la resolució escollida
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Inicialitzem el batch (Objecte que renderitza els sprites 2D)
        batch = new SpriteBatch();

        // Inicialitzem el bucket com a rectangle:
        // El centrem horitzontalment i l'apugem 20 pixels cap amunt de la y
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;
        // Li setegem width i height
        bucket.width = 64;
        bucket.height = 64;

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        if(bucket.x < 0) bucket.x = 0;
        if(bucket.x > 800 - 64) bucket.x = 800 - 64;

        raindrops = new Array<Rectangle>();
        spawnRaindrop();
    }

    public void render (float delta) {
        // Setegem un blau com a color del joc
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Es una bona practica actualitzar la camera per cada frame
        camera.update();

        // Renderitzem el bucket
        // Indiquem que el batch utilitzi el sistema de coordenades de la camera
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        puntuacio.draw(batch, "Punts: " + contador, 650, 370);
        batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();

        // Comprovem si es toca la pantalla o es clica el mouse
        if(Gdx.input.isTouched()) {
            // Guardarem la posició a un vector
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            // Transformem el sistema de coordenades al de la camera
            camera.unproject(touchPos);
            // Fiquem les coordenades al bucket
            bucket.x = touchPos.x - 64 / 2;
        }

        if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();
        //speed = 200 * Gdx.graphics.getDeltaTime();

        for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {

            Rectangle raindrop = iter.next();
            raindrop.y -= speed;
            if(raindrop.overlaps(bucket) && raindrop.y > (70 - speed)) {
                dropSound.play();
                iter.remove();
                contador++;
                if(contador % 10 == 0){
                    speed *= 1.2f;
                }
            }
            if(raindrop.y + 64 < 0) {
                iter.remove();
                rainMusic.stop();
                game.setScreen(new YouLost(game));

            }


        }

    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800-64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose () {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }
}
