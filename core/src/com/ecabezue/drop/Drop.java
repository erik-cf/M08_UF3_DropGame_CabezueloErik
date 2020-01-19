package com.ecabezue.drop;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;



public class Drop extends Game {

	public SpriteBatch batch;
	public BitmapFont introText;
	public BitmapFont punts_final;
	public long puntuacio;

	public void create() {
		// Inicialitzem el batch:
		batch = new SpriteBatch();
		// Inicialitzem les fonts:
		punts_final = new BitmapFont(Gdx.files.internal("punts.fnt"));
		punts_final.setColor(Color.TEAL);
		introText = new BitmapFont(Gdx.files.internal("introtext.fnt"));
		introText.setColor(Color.GOLDENROD);
		// Li diem que carregui el primer menu principal quan s'obri el joc:
		this.setScreen(new MainMenuScreen(this));
	}

	public void render() {
		super.render(); //important!
	}

	public void dispose() {
		batch.dispose();
		introText.dispose();
	}
}
