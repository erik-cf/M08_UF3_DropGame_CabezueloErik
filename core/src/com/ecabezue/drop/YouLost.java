package com.ecabezue.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class YouLost implements Screen {

    final Drop game;
    private Stage stage;

    OrthographicCamera camera;
    private Texture retry;

    Texture bg;

    private Sprite photoGameOverRestart;

    private Skin skin;

    public YouLost(final Drop game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        bg = new Texture(Gdx.files.internal("water-drop.jpg"));
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        retry = new Texture(Gdx.files.internal("retry.png"));

        photoGameOverRestart = new Sprite(retry);
        photoGameOverRestart.setSize(430, 160);
        photoGameOverRestart.setPosition((int)(800 - (430 * 1.5)), 10);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(bg, 0, 0, 800, 480);
        photoGameOverRestart.draw(game.batch);

        game.punts_final.draw(game.batch, "Has perdut :(! ", 550, 440);
        game.punts_final.draw(game.batch, "PUNTS: " + game.puntuacio, 550, 390);

        game.batch.end();

        if (Gdx.input.isTouched()) {
            Vector3 tmp=new Vector3(Gdx.input.getX(),Gdx.input.getY(), 0);
            camera.unproject(tmp);
            Rectangle textureBounds= new Rectangle(photoGameOverRestart.getX(),photoGameOverRestart.getY(),photoGameOverRestart.getWidth(),photoGameOverRestart.getHeight());
            if(textureBounds.contains(tmp.x,tmp.y)) {
                game.setScreen(new MainMenuScreen(game));
            }
        }
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
    public void dispose() {

    }
}
