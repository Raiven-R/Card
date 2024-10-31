package com.raiven.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {

    // Variables
    final Card game;
    AssetManager assetManager;

    Texture backgroundTexture;
    Texture spritesheetTexture;
    TextureRegion cardBackRegion;
    Array<TextureRegion> cardFrontRegions;
    Array<Sprite> cardSprites;
    Vector2 touchPos;
    Array<Rectangle> cardRectangles;
    boolean[] isFlipping;
    float[] rotationAngles;
    boolean[] showingFront;

    public GameScreen(final Card game) {
        this.game = game;
        assetManager = new AssetManager();

        // Load assets asynchronously
        assetManager.load("pokerTable3.jpg", Texture.class);
        assetManager.load("spritesheet.png", Texture.class); // Spritesheet for cards
        assetManager.load("cardback.png", Texture.class);

        // Initialize arrays for multiple cards
        cardFrontRegions = new Array<>();
        cardSprites = new Array<>();
        cardRectangles = new Array<>();
        isFlipping = new boolean[4];
        rotationAngles = new float[4];
        showingFront = new boolean[4];

        touchPos = new Vector2();
    }

    @Override
    public void show() {
        assetManager.finishLoading();

        // Retrieve loaded assets
        backgroundTexture = assetManager.get("pokerTable3.jpg", Texture.class);
        spritesheetTexture = assetManager.get("spritesheet.png", Texture.class);
        Texture cardBackTexture = assetManager.get("cardback.png", Texture.class);

        int cardWidth = 140;
        int cardHeight = 190;

        // Define a front region for each card
        for (int i = 0; i < 2; i++) { // Ensure loop runs twice for two cards
            TextureRegion frontRegion = new TextureRegion(spritesheetTexture, 0, 0, cardWidth, cardHeight);
            cardFrontRegions.add(frontRegion);
            Sprite cardSprite = new Sprite(frontRegion);
            cardSprite.setSize(0.8f, 0.9f);
            cardSprite.setOriginCenter();
            cardSprite.setPosition(100 + i * 150, 300); // Position cards in a row
            cardSprites.add(cardSprite);

            Rectangle cardRectangle = new Rectangle(cardSprite.getX(), cardSprite.getY(), cardSprite.getWidth(), cardSprite.getHeight());
            cardRectangles.add(cardRectangle);

            // Set all cards initially showing front
            showingFront[i] = true;
        }

        cardBackRegion = new TextureRegion(cardBackTexture);
    }

    @Override
    public void render(float delta) {
        input();
        logic(delta);
        draw();
    }

    public void input() {
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);

            for (int i = 0; i < cardSprites.size; i++) {
                if (cardRectangles.get(i).contains(touchPos.x, touchPos.y)) {
                    cardSprites.get(i).setPosition(
                        touchPos.x - cardSprites.get(i).getWidth() / 2,
                        touchPos.y - cardSprites.get(i).getHeight() / 2
                    );
                    cardRectangles.get(i).setPosition(cardSprites.get(i).getX(), cardSprites.get(i).getY());
                }
            }
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(touchPos);

            for (int i = 0; i < cardRectangles.size; i++) {
                if (cardRectangles.get(i).contains(touchPos.x, touchPos.y) && !isFlipping[i]) {
                    isFlipping[i] = true;
                }
            }
        }
    }

    public void logic(float delta) {
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        for (int i = 0; i < cardSprites.size; i++) {
            Sprite cardSprite = cardSprites.get(i);
            float cardWidth = cardSprite.getWidth();
            float cardHeight = cardSprite.getHeight();

            // Clamp the card position within screen bounds
            cardSprite.setX(MathUtils.clamp(cardSprite.getX(), 0, worldWidth - cardWidth));
            cardSprite.setY(MathUtils.clamp(cardSprite.getY(), 0, worldHeight - cardHeight));
            cardRectangles.get(i).set(cardSprite.getX(), cardSprite.getY(), cardWidth, cardHeight);

            // Flip animation logic
            if (isFlipping[i]) {
                rotationAngles[i] += 180 * delta;

                float scaleX = Math.abs(MathUtils.cosDeg(rotationAngles[i]));
                cardSprite.setScale(scaleX, 1f);

                if (rotationAngles[i] >= 90 && rotationAngles[i] < 180) {
                    cardSprite.setRegion(showingFront[i] ? cardBackRegion : cardFrontRegions.get(i));
                } else if (rotationAngles[i] >= 180) {
                    rotationAngles[i] = 0;
                    isFlipping[i] = false;
                    showingFront[i] = !showingFront[i];
                    cardSprite.setScale(1f, 1f);
                }
            }
        }
    }

    public void draw() {
        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        for (Sprite cardSprite : cardSprites) {
            cardSprite.draw(game.batch);
        }
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
