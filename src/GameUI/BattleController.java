package GameUI;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import DataModels.*;
import GameRules.BattleRules;
import UIFramework.NavigationController;

public class BattleController {
	
	private BattleRules rules;
	private int turn;
	private int unitRotation;
	private String gameState = "player: TurnStart";
	private Button[][] tiles;
	
	private ArrayList<BattleUnit> playerUnitList = new ArrayList<>();
	private ArrayList<BattleUnit> enemyUnitList = new ArrayList<>();
	
	@FXML Button playerUnit1;
	@FXML Button playerUnit2;
	@FXML Button playerUnit3;
	@FXML Label playerUnit1health;
	@FXML Label playerUnit2health;
	@FXML Label playerUnit3health;
	@FXML Label playerUnit1resource;
	@FXML Label playerUnit2resource;
	@FXML Label playerUnit3resource;
	
	@FXML Button enemyUnit1;
	@FXML Button enemyUnit2;
	@FXML Button enemyUnit3;
	@FXML Label enemyUnit1health;
	@FXML Label enemyUnit2health;
	@FXML Label enemyUnit3health;
	@FXML Label enemyUnit1resource;
	@FXML Label enemyUnit2resource;
	@FXML Label enemyUnit3resource;
	
	@FXML BorderPane battle;
	@FXML TilePane tilePane;
	@FXML Label overlord;
	@FXML Label instructions;
	@FXML HBox battleActions;
	@FXML Button attack;
	@FXML Button ability;
	@FXML Button heal;
	@FXML Button pass;
	
	
	/** <h1>Executed on first load of the view</h1>
	 * The method creates the game board 2D-button-array and enemy unit list,
	 * draws unit information on view, and prepares game board for play */
	@FXML private void initialize() {
		rules = new BattleRules();
		turn = 0;
		unitRotation = 0;
		
		createUnitLists();
		setUnitIcons();
		updateStats();
		setResourceColors();
		createBoard();
		
		// draw units in initial positions
		for (BattleUnit unit : enemyUnitList) {
			updateBoard(unit, tiles[0][0], tiles[unit.getxPos()][unit.getyPos()]);
		}
		for (BattleUnit unit : playerUnitList) {
			updateBoard(unit, tiles[0][0], tiles[unit.getxPos()][unit.getyPos()]);
		}
		
		battleActions.setVisible(false);
		overlord.setText("Overlord Level " + Player.level);
		
		turnStart();
	}

	/*===           === */
	/*=== UI events === */
	/*===           === */
	
	/** Send tile coordinates
	 * @param event button press from FXML */
	@FXML private void tilePress(ActionEvent event) {
		Button tile = (Button) event.getSource();
		int tileID = Integer.valueOf(tile.getId());
		int tileX = tileID / 10;
		int tileY = tileID % 10;
		
		switch (gameState) {
			case "player: Movement":
				handleMovement(tileX, tileY);
				break;
			case "player: attack":
				handleAction(tileX, tileY, "attack");
				break;
			case "player: ability":
				handleAction(tileX, tileY, "ability");
				break;
			case "player: heal":
				handleAction(tileX, tileY, "heal");
				break;
			default:
		}
	}
	
	/** Identify action button selected
	 * @param event button press from FXML */
	@FXML private void selectAction(ActionEvent event) {
		Button action = (Button) event.getSource();
		String actionID = action.getId();
		
		// hide action buttons after selection
		battleActions.setVisible(false);
		instructions.setVisible(true);
		
		selectAction(actionID);
	}

	/** Send view change request
	 * @param event button press from FXML */
	@FXML private void transitionView() {
		NavigationController.WINNERMSG = "You Lose!";
		NavigationController.loadView(NavigationController.MAINMENU);
	}

	/*===           === */
	/*=== UI update === */
	/*===           === */
	
	/** Update health and resource labels next to unit icons */
	private void updateStats() {
		playerUnit1health.setText("" + playerUnitList.get(0).health);
		playerUnit2health.setText("" + playerUnitList.get(1).health);
		playerUnit3health.setText("" + playerUnitList.get(2).health);
		playerUnit1resource.setText("" + playerUnitList.get(0).resource);
		playerUnit2resource.setText("" + playerUnitList.get(1).resource);
		playerUnit3resource.setText("" + playerUnitList.get(2).resource);
		
		enemyUnit1health.setText("" + enemyUnitList.get(0).health);
		enemyUnit2health.setText("" + enemyUnitList.get(1).health);
		enemyUnit3health.setText("" + enemyUnitList.get(2).health);
		enemyUnit1resource.setText("" + enemyUnitList.get(0).resource);
		enemyUnit2resource.setText("" + enemyUnitList.get(1).resource);
		enemyUnit3resource.setText("" + enemyUnitList.get(2).resource);
	}
	
	/** Update unit position on game board
	 * @param unit the active unit being updated
	 * @param oldTile the button where the unit currently is
	 * @param newTile the button where the unit needs to go */
	private void updateBoard(BattleUnit unit, Button oldTile, Button newTile) {
		oldTile.getStyleClass().remove(unit.getTeam());
		oldTile.getStyleClass().remove(unit.getType());
		newTile.getStyleClass().add(unit.getType());
		newTile.getStyleClass().add(unit.getTeam());
	}
	
	/** Remove dead units from the board
	 * @param unit a defeated unit */
	private void cleanCorpses(BattleUnit unit) {
		Button location = tiles[unit.getxPos()][unit.getyPos()];
		location.getStyleClass().remove(unit.getTeam());
		location.getStyleClass().remove(unit.getType());
	}
	
	/** Enable or disable action buttons as appropriate */
	private void showActionOptions() {
		// start with all buttons disabled
		attack.setDisable(true);
		ability.setDisable(true);
		heal.setDisable(true);
		
		// if there are targets in range to attack: enable attack button
		if (rules.validTargetsExist(activeUnit(), opponentUnitList(), "attack"))
			attack.setDisable(false);
		
		// if the unit is not a healer..
		if (!(activeUnit().getType().equals("water"))) {
			// if the unit has enough resource for its ability..
			if ((activeUnit().getType().equals("earth") && activeUnit().getResource() > 0) ||
				(activeUnit().getType().equals("fire") && activeUnit().getResource() >= 2) ) {
				// as long as a target is in range for an ability: enable ability button
				if (rules.validTargetsExist(activeUnit(), opponentUnitList(), "ability")) 
					ability.setDisable(false);
			}
		// if the unit is a healer..
		} else {
			// if the unit has enough resource to heal..
			if (activeUnit().getType().equals("water") && activeUnit().getResource() >= 1) {
				// if there are targets in range to heal: enable heal button
				if (rules.validTargetsExist(activeUnit(), friendlyUnitList(), "heal"))
					heal.setDisable(false);
			}
			// if the unit has enough resource for its ability: enable ability button
			if (activeUnit().getType().equals("water") && activeUnit().getResource() >= 2)
				ability.setDisable(false);
		}
	}

	/** Remove all visual highlights from all tiles */
	private void removeHighlights() {
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				tiles[row][col].getStyleClass().remove("blueHighlight");
				tiles[row][col].getStyleClass().remove("redHighlight");
				tiles[row][col].getStyleClass().remove("greenHighlight");
			}
		}
	}
	
	/** Visually highlight tiles valid for moves
	 * @param moveList 2D boolean array where true elements are valid moves */
	private void showValidMoves(boolean[][] moveList) {
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (moveList[row][col])
					tiles[row][col].getStyleClass().add("blueHighlight");
			}
		}
	}

	/** Visually highlight tiles occupied by valid targets
	 * @param targetArray 2D boolean array where true elements are valid target locations
	 * @param team unit team targetable by action*/
	private void showValidTargets(boolean[][] targetArray, String team) {
		// apply highlights
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (targetArray[row][col]) {
					if (team.equals(opponentTeam()))			// enemies highlighted red
						tiles[row][col].getStyleClass().add("redHighlight");
					else									// friendlies highlighted green
						tiles[row][col].getStyleClass().add("greenHighlight");
				}
			}
		}
	}
	
	/*===            === */
	/*=== Game  Flow === */
	/*===            === */
	
	/** Turns for both players begin here */
	private void turnStart() {
		switch (gameState) {
			case "player: TurnStart":
				// verify unit is alive and able to act
				if (unitAlive(activeUnit())) {
					// update instructions for player to move
					instructions.setText("Select a tile to move");
					// present move options
					showValidMoves(rules.isMoveValid(activeUnit().getxPos(), activeUnit().getyPos(), friendlyUnitList(), opponentUnitList()));
					gameState = "player: Movement";  // game waits for player input after this
				} else {
					
				}
				break;
				
			case "enemy: TurnStart":
				if (unitAlive(activeUnit()))
					aiControl();
				else
					nextTurn();
				break;
				
			default:
		}
	}
	
	/** Ends a turn, then prepares for and begins the next */
	private void nextTurn() {
		// end the game if a team is defeated
		victoryCheck();
		
		// after every turn
		updateStats();
		removeHighlights();
		gameState = opponentTeam() + ": TurnStart";
		battle.setMouseTransparent(!battle.isMouseTransparent());
		turn++;
		// after player and enemy both get turns
		if (turn % 2 == 0)
			unitRotation++;
		
		// next
		turnStart();
	}
	
	/** Verify and execute unit movement
	 * @param newX x-coordinate of destination tile
	 * @param newY y-coordinate of destination tile */
	private void handleMovement(int newX, int newY) {
		
		// ignore moves outside highlights
		boolean validMove = tiles[newX][newY].getStyleClass().contains("blueHighlight");
		if (!validMove)
			return;
		
		// execute move
		Button oldTile = tiles[activeUnit().getxPos()][activeUnit().getyPos()];
		Button newTile = tiles[newX][newY];
		updateBoard(activeUnit(), oldTile, newTile);
		activeUnit().setxPos(newX);
		activeUnit().setyPos(newY);
		
		// clean up after successful move and prepare next phase
		removeHighlights();
		if (activeTeam().equals("player")) {
			instructions.setVisible(false);
			battleActions.setVisible(true);
		}
		gameState = activeTeam() + ": SelectAction";
		showActionOptions();
	}
	
	/** Prepare target list for selected action
	@param action action to be performed */
	private void selectAction(String action) {
		
		if (action.equals("pass")) {
			// skip the rest of targeting process
			nextTurn();
			return;
		}
		
		// update instructions for player to select a target
		instructions.setText("Select a target");
		
		if(action.equals("attack") || !(activeUnit().getType().equals("water")))
			showValidTargets(rules.isActionValid(activeUnit(), opponentUnitList(), action), opponentTeam());
		else
			showValidTargets(rules.isActionValid(activeUnit(), friendlyUnitList(), action), activeTeam());
		gameState = activeTeam() + ": " + action;
	}
	
	/** Verify and execute unit action
	 * @param activeUnit unit performing the action
	 * @param targetX x-coordinate of target tile
	 * @param targetY y-coordinate of target tile
	 * @param action action to be performed */
	private void handleAction(int targetX, int targetY, String action) {
		
		ArrayList<BattleUnit> targetList;
		if (tiles[targetX][targetY].getStyleClass().contains("redHighlight"))
			targetList = opponentUnitList();
		else if (tiles[targetX][targetY].getStyleClass().contains("greenHighlight"))
			targetList = friendlyUnitList();
		else // only highlighted tiles contain valid targets, ignore the rest
			return;
		
		BattleUnit targetUnit = null;
		// get the unit on selected tile
		for (BattleUnit unit : targetList) {
			if (unit.getxPos() == targetX && unit.getyPos() == targetY) {
				targetUnit = unit;
				break;
			}
		}
		
		// validate action and execute
		switch (action) {
			case "attack":
				// apply damage changes
				targetUnit.modHealth(-activeUnit().getDamage());
				if (targetUnit.getHealth() < 0)
					targetUnit.setHealth(0);
				// apply resource changes
				if (activeUnit().getType().equals("earth"))
					activeUnit().setResource(activeUnit().getResource() + 1);
				break;
			case "ability":
				switch (activeUnit().getType()) {
					case "fire":
						targetUnit.modHealth(-activeUnit().getDamage() * 2);
						if (targetUnit.getHealth() < 0)
							targetUnit.setHealth(0);
						activeUnit().modResource(-2);
						break;
					case "earth":
						// apply damage equal to amount of rage
						targetUnit.modHealth(-activeUnit().getResource());
						if (targetUnit.getHealth() < 0)
							targetUnit.setHealth(0);
						// remove all rage
						activeUnit().setResource(0);
						break;
					case "water":
						// apply heal to all friendly team units
						for (BattleUnit unit : friendlyUnitList()) {
							unit.modHealth(2);
							// prevent over-healing
							if (unit.getHealth() > (unit.getType().equals("earth") ? 15 : 10))
								unit.setHealth(unit.getType().equals("earth") ? 15 : 10);
						}
						// apply resource cost
						activeUnit().modResource(-2);
						break;
					default:
				}
				break;
			case "heal":
				// apply heal to target
				targetUnit.modHealth(3);
				// prevent over-healing
				if (targetUnit.getHealth() > (targetUnit.getType().equals("earth") ? 15 : 10))
					targetUnit.setHealth(targetUnit.getType().equals("earth") ? 15 : 10);
				// apply resource cost
				activeUnit().modResource(-1);
				break;
			default:
		}
		
		if (!unitAlive(targetUnit))
			cleanCorpses(targetUnit);
		
		nextTurn();
	}
	
	/** AI turn control */
	private void aiControl() {
		// TODO make AI work (g'luck)
		
		// inform player to wait for AI
		instructions.setText("It is the enemy's turn..");
		
		gameState = "enemy: Movement";
		showValidMoves(rules.isMoveValid(activeUnit().getxPos(), activeUnit().getyPos(), friendlyUnitList(), opponentUnitList()));
		
		PauseTransition wait = new PauseTransition(Duration.millis(300));
		wait.onFinishedProperty().set(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					nextTurn();
				}
		});
		wait.play();
	}
	
	/** Check the opponent team's condition */
	private void victoryCheck() {
		boolean victory = rules.isEnemyDefeated(opponentUnitList());
		
		if (victory) {
			if (activeTeam().equals("player")) {
				Player.currency += 15 + 5 * Player.level;
				Player.level += 1;
				NavigationController.WINNERMSG = "You Win!";
			} else {
				Player.currency += 5;
				NavigationController.WINNERMSG = "You Lose!";
			}
			NavigationController.loadView(NavigationController.MAINMENU);
		}
	}

	/*===            === */
	/*=== Game setup === */
	/*===            === */
	
	/** Generate a fresh list of enemy units for a battle */
	private void createUnitLists() {
		enemyUnitList.clear();
		enemyUnitList.add(new BattleUnit("earth", 1, 15, 0, "enemy", 1, 1));
		enemyUnitList.add(new BattleUnit("fire", 3, 10, 15, "enemy", 1, 3));
		enemyUnitList.add(new BattleUnit("water", 2, 10, 10, "enemy", 1, 5));
		
		// TODO find a better way, this seems crude..
		playerUnitList.clear();
		playerUnitList.add(new BattleUnit(Player.unitList.get(0).getType(), Player.unitList.get(0).getDamage(),
				Player.unitList.get(0).getHealth(), Player.unitList.get(0).getResource(), "player", 6, 2));
		playerUnitList.add(new BattleUnit(Player.unitList.get(1).getType(), Player.unitList.get(1).getDamage(),
				Player.unitList.get(1).getHealth(), Player.unitList.get(1).getResource(), "player", 6, 4));
		playerUnitList.add(new BattleUnit(Player.unitList.get(2).getType(), Player.unitList.get(2).getDamage(),
				Player.unitList.get(2).getHealth(), Player.unitList.get(2).getResource(), "player", 6, 6));
	}
	
	/** Draw the appropriate icon for each unit on the side panels */
	private void setUnitIcons() {
		
		playerUnit1.getStyleClass().add(playerUnitList.get(0).type);
		playerUnit2.getStyleClass().add(playerUnitList.get(1).type);
		playerUnit3.getStyleClass().add(playerUnitList.get(2).type);
		enemyUnit1.getStyleClass().add(enemyUnitList.get(0).type);
		enemyUnit2.getStyleClass().add(enemyUnitList.get(1).type);
		enemyUnit3.getStyleClass().add(enemyUnitList.get(2).type);
	}
	
	/** Color the resource label according to unit type */
	private void setResourceColors() {
		enemyUnit1resource.getStyleClass().add((enemyUnitList.get(0).getType().equals("earth") ? "rage" : "mp"));
		enemyUnit2resource.getStyleClass().add((enemyUnitList.get(1).getType().equals("earth") ? "rage" : "mp"));
		enemyUnit3resource.getStyleClass().add((enemyUnitList.get(2).getType().equals("earth") ? "rage" : "mp"));
		playerUnit1resource.getStyleClass().add((playerUnitList.get(0).getType().equals("earth") ? "rage" : "mp"));
		playerUnit2resource.getStyleClass().add((playerUnitList.get(1).getType().equals("earth") ? "rage" : "mp"));
		playerUnit3resource.getStyleClass().add((playerUnitList.get(2).getType().equals("earth") ? "rage" : "mp"));
	}
	
	/** Initialize the 2D button array representing the game board */
	private void createBoard() {
		tiles = new Button[8][8];
		 
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Button tile = new Button();
				tile.setId(row + "" + col);
				tile.getStyleClass().add("tile");
				if ((row + col) % 2 == 0)
					tile.getStyleClass().add("whtTile");
				else
					tile.getStyleClass().add("blkTile");
				tile.setOnAction(this::tilePress);
				
				tilePane.getChildren().add(tile);
				tiles[row][col] = tile;
			}
		}
	}

	/*===             === */
	/*=== Information === */
	/*===             === */
	
	/** Calculates the unit that is active for this turn
	 * @return the current turn's active unit */
	private BattleUnit activeUnit() {
		BattleUnit activeUnit;
		int unitIndex = unitRotation % 3;
		if (turn % 2 == 0)
			activeUnit = playerUnitList.get(unitIndex);
		else
			activeUnit = enemyUnitList.get(unitIndex);
		
		return activeUnit;
	}
	
	/** finds the team of the unit that is currently active
	 * @return team name */
	private String activeTeam() {
		if (turn % 2 == 0)
			return "player";
		return "enemy";
	}
	
	/** finds the opponent team of the unit that is currently active
	 * @return team name */
	private String opponentTeam() {
		if (turn % 2 == 0)
			return "enemy";
		return "player";
	}
	
	/** finds the list of units for the team opposing the active unit
	 * @return the unit list of the opposing team */
	private ArrayList<BattleUnit> opponentUnitList() {
		if (activeTeam().equals("enemy"))
			return playerUnitList;
		return enemyUnitList;
	}
	
	/** finds the list of units for the team of the active unit
	 * @return the unit list of the active team */
	private ArrayList<BattleUnit> friendlyUnitList() {
		if (activeTeam().equals("player"))
			return playerUnitList;
		return enemyUnitList;
	}
	
	/** Check if the unit is alive or defeated
	 * @return true if the unit is alive */
	private boolean unitAlive(BattleUnit unit) {
		if (unit.getHealth() > 0)
			return true;
		return false;
	}
}