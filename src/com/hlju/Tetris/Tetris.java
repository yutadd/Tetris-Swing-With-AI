package com.hlju.Tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
/**テトリスのコンテンツを表示するコンポーネント*/
public class Tetris extends JPanel {
	/**参照されません*/
	private static final long serialVersionUID = -807909536278284335L;
	private static final int BlockSize = 10;
	/**プレイ画面のブロックを積み上げる部分の幅*/
	private static final int BlockWidth = 16;
	/**プレイ画面のブロックを積み上げる部分の高さ*/
	private static final int BlockHeigth = 26;
	//順位の管理
	static String zyuni="";
	/**画面の更新頻度*/
	private static final int TimeDelay = 100;
	/**次のブロック下に表示される著作者情報*/
	private static final String[] AuthorInfo = {
			"©","HelloClydeと坂島"
	};

	/**プレイ画面のドットを管理する配列*/
	private boolean[][] BlockMap = new boolean[BlockHeigth][BlockWidth];

	/**スコア*/
	private int Score = 0;

	/**中断しているか否か*/
	private boolean IsPause = false;

	/**ブロックの形x,yが入った配列[[[bool...],[bool...]]]*/
	static boolean[][][] Shape = BlockV4.Shape;

	/**今のブロックの位置*/
	private Point NowBlockPos;

	/**今のブロックのx,y配列*/
	private boolean[][] NowBlockMap;
	/**次のブロックのx,y配列*/
	private boolean[][] NextBlockMap;
	/**
	 *ブロックの向き情報
	 */
	private int NextBlockState;
	private int offset=0;
	private int NowBlockState;

	/*タイマーインスタンス・中断したりするためにこのスコープで宣言する必要があるようだ*/
	private Timer timer;

	/*導いた最適解*/
	Point point;
	/*導いた手順を順番に実行するためのインデックス*/
	Point controllIndex=new Point();
	/**盤面を評価するための評価関数
	 * 上に行くほど減点を高くする。*/
	int analyze(boolean BlockMap[][]) {
		int score=BlockMap.length*BlockMap[0].length;
		for(int i=0;i<BlockMap.length;i++) {
			for(int a=0;a<BlockMap[i].length;a++) {
				if(BlockMap[i][a]) {
					score-=(BlockMap.length-i);
				}
			}
		}
		return score;
	}
	Point find() {
		int max=-(BlockMap.length*BlockMap[0].length*BlockMap.length);
		int max_x=0;
		int update=0;
		int max_y=0;
		for(int i=CalNewBlockInitPos().x;i<BlockMap[i].length;i++){//左端から右端
			for(int a=1;a<4;a++) {
				boolean[][] _shape = copy2d(NowBlockMap);
				_shape=RotateBlock(_shape, a);
				int j=0;
				boolean[][] _BlockMap =copy2d(BlockMap);
				if(!IsTouch(_shape, new Point(i,0))) {
					
					for(;!IsTouch(_shape,new Point(i,j+1));j++);
					FixBlock(_BlockMap,_shape,new Point(i,j));
					int score=analyze(_BlockMap);
					System.out.println(max+"/"+(BlockMap.length*BlockMap[0].length));
					if(max<score) {
						max=score;
						max_x=i;
						max_y=a;
						update++;
						//System.out.println("提案");
						//ShowMap(_BlockMap);
					}
				}
			}
		}
		controllIndex=new Point(0,0);
		if(update==0) {
			System.out.println(max+"/"+(BlockMap.length*BlockMap[0].length));
			ShowMap(NowBlockMap);
			ShowMap(BlockMap);
		}
		return new Point(max_x,max_y);
	}
	public Tetris(int offset) {

		this.setBackground(Color.orange);
		this.setSize(300,500);
		this.offset=offset;
		/***/
		this.Initial();
		/**描画ごとにアクションイベントを生成する*/
		timer = new Timer(Tetris.TimeDelay, this.TimerListener);
		timer.start();
		Thread th=new Thread() {
			@Override
			public void run() {
				Point DesPoint;
				for(;;) {
					if(!IsPause) {
						while(controllIndex.x<point.x||controllIndex.y<point.y) {
							if(controllIndex.x<point.x) {
								controllIndex.x++;
								System.out.print("→");
								DesPoint = new Point(Tetris.this.NowBlockPos.x + 1, Tetris.this.NowBlockPos.y);
								if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)){
									Tetris.this.NowBlockPos = DesPoint;
								}
							}
							if(controllIndex.y<point.y) {
								controllIndex.y++;
								System.out.print("🌀");
								boolean[][] TurnBlock = Tetris.this.RotateBlock(Tetris.this.NowBlockMap,1);
								if (!Tetris.this.IsTouch(TurnBlock, Tetris.this.NowBlockPos)){
									Tetris.this.NowBlockMap = TurnBlock;
								}
								
							}
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								// TODO 自動生成された catch ブロック
								e.printStackTrace();
							}
						}
						DesPoint = new Point(Tetris.this.NowBlockPos.x, Tetris.this.NowBlockPos.y + 1);
						if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)){
							Tetris.this.NowBlockPos = DesPoint;
						}
					}
					//System.out.println(Tetris.this.NowBlockPos);
					repaint();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}

				}
			}
		};
		th.start();
		//this.addKeyListener(this.KeyListener);
	}

	public void SetMode(String mode){
		if (mode.equals("v6")){
			Tetris.Shape = BlockV6.Shape;
		}
		else{
			Tetris.Shape = BlockV4.Shape;
		}
		this.Initial();
		this.repaint();
	}

	/**
	 *	次のブロックを生成と登録するメソッド。
	 */
	private void getNextBlock() {

		this.NowBlockState = this.NextBlockState;
		this.NowBlockMap = this.NextBlockMap;
		this.NextBlockState = this.CreateNewBlockState();
		this.NextBlockMap = this.getBlockMap(NextBlockState);
		this.NowBlockPos = this.CalNewBlockInitPos();
		System.out.println("nextShape");
		ShowMap(NowBlockMap);
		point=find();
		System.out.println("point");
		System.out.println(point);
	}

	/**
	 *壁にぶつかるかどうか検査する
	 * @return
	 */
	private boolean IsTouch(boolean[][] SrcNextBlockMap,Point SrcNextBlockPos) {
		for (int i = 0; i < SrcNextBlockMap.length;i ++){
			for (int j = 0;j < SrcNextBlockMap[i].length;j ++){
				if (SrcNextBlockMap[i][j]){
					if (SrcNextBlockPos.y + i >= Tetris.BlockHeigth || SrcNextBlockPos.x + j < 0 || SrcNextBlockPos.x + j >= Tetris.BlockWidth){
						return true;
					}
					else{
						if (SrcNextBlockPos.y + i < 0){
							continue;
						}
						else{
							if (this.BlockMap[SrcNextBlockPos.y + i][SrcNextBlockPos.x + j]){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * ブロックの固定
	 */
	private boolean FixBlock(boolean[][] BlockMap,boolean[][] NowBlockMap,Point NowBlockPos){
		for (int i = 0;i < NowBlockMap.length;i ++){
			for (int j = 0;j <NowBlockMap[i].length;j ++){
				if (NowBlockMap[i][j])
					if (NowBlockPos.y + i < 0)
						return false;
					else
						BlockMap[NowBlockPos.y + i][NowBlockPos.x + j] = NowBlockMap[i][j];
			}
		}
		return true;
	}

	/**
	 * ブロックの中心を求める。
	 * @return 中心のPoint
	 */
	private Point CalNewBlockInitPos(){
		return new Point(/*Tetris.BlockWidth / 2 -this.NowBlockMap[0].length / 2*/ 0, - this.NowBlockMap.length);
	}

	/**
	 * ゲームの開始*/
	public void Initial() {
		//System.out.println("initial");
		/**表示される積みあがている可能性のあるブロックの配列をクリア*/
		for (int i = 0;i < this.BlockMap.length;i ++){
			for (int j = 0;j < this.BlockMap[i].length;j ++){
				this.BlockMap[i][j] = false;
			}
		}
		//スコアの初期化
		this.Score = 0;
		// 一番最初のユーザーが操作するブロックを選ぶ
		this.NowBlockState = this.CreateNewBlockState();
		//選んだブロックから具体的な２次元配列を取得する。
		this.NowBlockMap = this.getBlockMap(this.NowBlockState);
		//その次にユーザーが操作するブロックを選ぶ
		this.NextBlockState = this.CreateNewBlockState();
		//選んだブロックから具体的な２次元配列を取得する。
		this.NextBlockMap = this.getBlockMap(this.NextBlockState);
		this.NowBlockPos = this.CalNewBlockInitPos();
		System.out.println("nextShape");
		ShowMap(NowBlockMap);
		point=find();
		System.out.println("point");
		System.out.println(point);
		this.repaint();
	}
	/**タイマーをストップする。
	 * これにより、定期イベントが呼ばれなくなる*/
	public void SetPause(boolean value){
		this.IsPause = value;
		if (this.IsPause){
			this.timer.stop();
		}
		else{
			this.timer.restart();
		}
		this.repaint();
	}

	/**
	 *次のブロックの番号を選ぶ!
	 */
	private int CreateNewBlockState() {
		int Sum = Tetris.Shape.length * 4;
		int ret= (int) (Math.random() * 1000) % Sum;
		//System.out.println("state :"+ret);
		return ret;
	}
	/**ブロックの番号からブロックを取得する*/
	private boolean[][] getBlockMap(int BlockState) {
		int Shape = BlockState / 4;
		int Arc = BlockState % 4;
		//System.out.println(BlockState + "," + Shape + "," + Arc);
		return this.RotateBlock(Tetris.Shape[Shape], Arc);
	}
	/**ブロックの回転を行う。*/
	private boolean[][] RotateBlock(boolean[][] shape, int time) {
		/*if(time == 0) {
			return shape;
		}*/
		int heigth = shape.length;
		int width = shape[0].length;
		boolean[][] ResultMap = new boolean[heigth][width];
		int tmpH = heigth - 1, tmpW = 0;
		for(int i = 0; i < heigth && tmpW < width; i++) {
			for(int j = 0; j < width && tmpH > -1; j++) {
				ResultMap[i][j] = shape[tmpH][tmpW];
				tmpH--;
			}
			tmpH = heigth - 1;
			tmpW++;
		}
		for(int i = 1; i < time; i++) {
			ResultMap = RotateBlock(ResultMap, 0);
		}
		return ResultMap;
	}

	/**
	 * 回転のテストを行っているっぽい
	 * @param args
	 */
	/*static public void main(String... args) {
		boolean[][] SrcMap = Tetris.Shape[3];
		Tetris.ShowMap(SrcMap);
		Tetris tetris = new Tetris(0);
		boolean[][] result = tetris.RotateBlock(SrcMap, 1);
		Tetris.ShowMap(result);

	}*/

	/**
	 * ２次元配列の形式で渡されたブロックの形を出力する
	 * @param SrcMap
	 */
	static private void ShowMap(boolean[][] SrcMap){
		System.out.println("-----");
		for (int i = 0;i < SrcMap.length;i ++){
			for (int j = 0;j < SrcMap[i].length;j ++){
				if (SrcMap[i][j])
					System.out.print("■");
				else
					System.out.print("□");
			}
			System.out.println();
		}
		System.out.println("-----");
	}
	static private boolean[][] copy2d(boolean[][] src){
		boolean[][] result=new boolean[src.length][src[0].length];
		for(int i=0;i<src.length;i++) {
			for(int a=0;a<src[i].length;a++) {
				result[i][a]=src[i][a];
			}
		}
		return result;
	}

	/**
	 * 画面の描画描画を担当するメソッド。
	 * nowBlockMapの情報より描画をする。
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(offset!=50) {
			//   ǽ
			for (int i = 0; i < Tetris.BlockHeigth + 1; i++) {
				g.drawRect(0 * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize, Tetris.BlockSize);
				g.drawRect((Tetris.BlockWidth + 1) * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize,
						Tetris.BlockSize);
			}
			for (int i = 0; i < Tetris.BlockWidth; i++) {
				g.drawRect((1 + i) * Tetris.BlockSize, Tetris.BlockHeigth * Tetris.BlockSize, Tetris.BlockSize,
						Tetris.BlockSize);
			}
			//     ǰ    
			for (int i = 0; i < this.NowBlockMap.length; i++) {
				for (int j = 0; j < this.NowBlockMap[i].length; j++) {
					if (this.NowBlockMap[i][j])
						g.fillRect((1 + this.NowBlockPos.x + j) * Tetris.BlockSize, (this.NowBlockPos.y + i) * Tetris.BlockSize,
								Tetris.BlockSize, Tetris.BlockSize);
				}
			}
			//    Ѿ  ̶  ķ   
			for (int i = 0; i < Tetris.BlockHeigth; i++) {
				for (int j = 0; j < Tetris.BlockWidth; j++) {
					if (this.BlockMap[i][j])
						g.fillRect(Tetris.BlockSize + j * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize,
								Tetris.BlockSize);
				}
			}
			//      һ      
			for (int i = 0;i < this.NextBlockMap.length;i ++){
				for (int j = 0;j < this.NextBlockMap[i].length;j ++){
					if (this.NextBlockMap[i][j])
						g.fillRect(190 + j * Tetris.BlockSize, 30 + i * Tetris.BlockSize, Tetris.BlockSize, Tetris.BlockSize);
				}
			}
			//           Ϣ
			g.drawString("スコア:" + this.Score, 190, 10);
			for (int i = 0;i < Tetris.AuthorInfo.length;i ++){
				g.drawString(Tetris.AuthorInfo[i], 190, 100 + i * 20);
			}

			//      ͣ
			if (this.IsPause){
				g.setColor(Color.white);
				g.fillRect(70, 100, 50, 20);
				g.setColor(Color.black);
				g.drawRect(70, 100, 50, 20);
				g.drawString("PAUSE", 75, 113);
			}
		}
	}
	/**
	 * 積み上げられたブロックをスキャンし、そろってるラインを削除する。
	 * 
	 * @return そろったラインによる得点を返却する。
	 */
	private int ClearLines(){
		int lines = 0;
		for (int i = 0;i < this.BlockMap.length;i ++){
			boolean IsLine = true;
			for (int j = 0;j < this.BlockMap[i].length;j ++){
				if (!this.BlockMap[i][j]){
					IsLine = false;
					break;
				}
			}
			if (IsLine){
				for (int k = i;k > 0;k --){
					this.BlockMap[k] = this.BlockMap[k - 1];
				}
				this.BlockMap[0] = new boolean[Tetris.BlockWidth];
				lines ++;
			}
		}
		return lines;
	}

	/**画面の更新時に毎回呼ばれるリスナー。
	 * 各種判定等をここから行う。
	 * ゲームオーバー時には、ランキングサーバーに得点を送信し、そこから順位を取得する
	 *通信について＞＞
						TCPの25565ポート（僕の好きなマイクラのサーバーのデフォルトポート番号を使用してます！）
						各種例外処理は最低限の実装になります。
	 */
	ActionListener TimerListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO  Զ    ɵķ      
			if (Tetris.this.IsTouch(Tetris.this.NowBlockMap, new Point(Tetris.this.NowBlockPos.x, Tetris.this.NowBlockPos.y + 1))){
				
				boolean fb=Tetris.this.FixBlock(BlockMap,NowBlockMap,NowBlockPos);
				//System.out.println("結果");
					//ShowMap(BlockMap);
				if (fb){
					
					Tetris.this.Score += Tetris.this.ClearLines() * 10;
					Tetris.this.getNextBlock();
					
				}
				else{
					//JOptionPane.showMessageDialog(Tetris.this.getParent(), "GAME OVER");
					/*try {
						Socket s=new Socket("localhost",25565);
						Thread th=new Thread() {
							@Override
							public void run() {
								try {
									BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
									String str;
									s.getOutputStream().write(("submit,"+Tetris.this.Score+"\r\n").getBytes());
									s.getOutputStream().flush();
									while((str=br.readLine())!=null) {
										System.out.println("message :"+str);
										if(str.startsWith("r,")) {
											String[] args=str.split(",");
											zyuni=args[1];
										}
										break;
									}
									s.close();

								}catch(Exception e) {e.printStackTrace();}
							}
						};
						th.start();
						th.join();
						//スコープがthスレッドの内部だと描画が正常に行われないため、ポップアップは以下で行う
						if(!zyuni.equals("")) {
							JOptionPane.showMessageDialog(Tetris.this,
									zyuni+"位に入賞しました！");
						}else {
							JOptionPane.showMessageDialog(Tetris.this,"ランキングサーバーとの通信でエラーが発生しました");
						}
						System.out.println("接続終了。");
					}catch(Exception e) {JOptionPane.showMessageDialog(Tetris.this,
							"ランキングサーバーに接続できませんでした");e.printStackTrace();
					}*/
					Tetris.this.Initial();
				}

			}
			else{
				Tetris.this.NowBlockPos.y ++;
			}
			Tetris.this.repaint();
		}
	};

	//キーボードからの入力を受け付けるリスナー。入力があるたびに描画しなおす。
	/*java.awt.event.KeyListener KeyListener = new java.awt.event.KeyListener(){

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO  Զ    ɵķ      
			if (!IsPause){
				Point DesPoint;
				//switch (e.getKeyCode()) {
			switch(new Random().nextInt(40)+37)	{
			case KeyEvent.VK_DOWN:
					DesPoint = new Point(Tetris.this.NowBlockPos.x, Tetris.this.NowBlockPos.y + 1);
					if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)){
						Tetris.this.NowBlockPos = DesPoint;
					}
					break;
				case KeyEvent.VK_UP:
					boolean[][] TurnBlock = Tetris.this.RotateBlock(Tetris.this.NowBlockMap,1);
					if (!Tetris.this.IsTouch(TurnBlock, Tetris.this.NowBlockPos)){
						Tetris.this.NowBlockMap = TurnBlock;
					}
					break;
				case KeyEvent.VK_RIGHT:
					DesPoint = new Point(Tetris.this.NowBlockPos.x + 1, Tetris.this.NowBlockPos.y);
					if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)){
						Tetris.this.NowBlockPos = DesPoint;
					}
					break;
				case KeyEvent.VK_LEFT:
					DesPoint = new Point(Tetris.this.NowBlockPos.x - 1, Tetris.this.NowBlockPos.y);
					if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)){
						Tetris.this.NowBlockPos = DesPoint;
					}
					break;
				}
				//System.out.println(Tetris.this.NowBlockPos);
				repaint();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO  Զ    ɵķ      

		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO  Զ    ɵķ      

		}

	};*/
}
