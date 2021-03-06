package com.taojiang.cpu;
import com.taojiang.queue.Process;
import com.taojiang.schedule.LanchWnd;
import com.taojiang.schedule.ScheduleController;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
public class CPU {
	public final int CPU_STATE_SPARE = 0;  // cpu空闲
	public final int CPU_STATE_WORK = 1;   // cpu工作

	public String cpuName;                     // cpu名称
	public Process process1;                   // cpu当前处理的进程1
	public Process process2;                   // cpu当前处理的进程2
	public int tracks;                         // 单个CPU的道数
	public int state;                          // cpu当前状态
	public ScheduleController scheduleController;

	public CPU(String cpuName,ScheduleController scheduleController){
		this.cpuName = cpuName;
		process1 = null;
		process2 = null;
		state = CPU_STATE_SPARE;
		this.scheduleController = scheduleController;
	}
	// 设置CPU处理的进程
	public void setProcess(Process process){
		int which;
		System.out.println(getCpuName());
		if(process1 == null){
			process1 = process;
			// 更新此cpu的状态
			updateState();
			// 确定在那个道上执行
			if(getCpuName().equals("cpu1")){
				which = 0;
			}else{
				which = 2;
			}
		}else{
			process2 = process;
			updateState();
			if(getCpuName().equals("cpu1")){
				which = 1;
			}else{
				which = 3;
			}
		}
		Thread thread = new Thread(new Runnable() {
			final int whichPB = which;
			@Override
			public void run() {
				animation(process,whichPB);
			}
		});

		thread.start();

	}

	private void updateState() {
		// TODO Auto-generated method stub
		if(process1 != null && process2 != null)
			state = CPU_STATE_WORK;
		else
			state = CPU_STATE_SPARE;
		updateUI(state);
		System.out.println(getCpuName() + "状态更新成功" + state);
	}

	private void updateUI(int state){
		ImageView imageview;
		if(getCpuName().equals("cpu1"))
			imageview = scheduleController.iv_cpu1;
		else
			imageview = scheduleController.iv_cpu2;

		switch (state) {
		case CPU_STATE_SPARE:
			imageview.setImage(new Image(ScheduleController.image_url_1));
			break;
		case CPU_STATE_WORK:
			imageview.setImage(new Image(ScheduleController.image_url_2));
			break;
		default:
			break;
		}
	}
	public String getCpuName() {
		// TODO Auto-generated method stub
		return cpuName;
	}
	//产生进度条动画的函数
	protected void animation(Process process,int which) {
		switch (which) {
		case 0:
			animation(scheduleController.pb_cpu1_1, scheduleController.lb_cpu1_1, process, 0);
			break;
		case 1:
			animation(scheduleController.pb_cpu1_2, scheduleController.lb_cpu1_2, process, 1);
			break;
		case 2:
			animation(scheduleController.pb_cpu2_1, scheduleController.lb_cpu2_1, process, 2);
			break;
		case 3:
			animation(scheduleController.pb_cpu2_2, scheduleController.lb_cpu2_2, process, 3);
			break;
		default:
			break;
		}
	}

	public void animation(ProgressBar pb,Label lb,Process process,int which){
		final int totalTime = process.getProRuntime();
		// 进程开始执行前
		// 分配主存空间操作可在主窗口控制器里边实现
		// 界面绘图

		// 进程开始执行
		 Animation animation = new Transition() {
		     {
		         setCycleDuration(Duration.millis(totalTime*1000));
		     }

		     protected void interpolate(double frac) {
		         final int n = Math.round(totalTime * (float) frac);
//		         System.out.println(frac + " " + n);
		         lb.setText(""+Math.round(totalTime * (float)(1- frac)));
//		         System.out.println(pb==null?"pb si null":"pb is not null" + which);
		         pb.setProgress(frac);
		         if(frac == 1.0){
		        	 lb.setText("0");
		        	 pb.setProgress(0F);
		        	// 进程完成，回收主存，相邻区间合并
					// 界面UI刷新
//		        	 System.out.println(process.getProName() == null);
					scheduleController.table.recycleMemory(process.getProName());
					if(which % 2 == 0)
						process1 = null;
					else
						process2 = null;
					updateState();
		         }
		     }

		 };
		 animation.play();
	}
}