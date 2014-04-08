package com.onlan.servlet;

import java.awt.Font;
import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/** 
 * @author Pecan 
 * 类说明 
 */
public class LoginChartServlet extends MultiActionController{
	public ModelAndView list(HttpServletRequest request,HttpServletResponse response){
		TimeSeries timeSeries = new TimeSeries("链接量统计");
		//时间曲线数据集合
		TimeSeriesCollection lineDataset = new TimeSeriesCollection();
		//构造数据集合
		for (Entry<Date, Integer> entry : UserLoginRunnable.hashMap.entrySet()) {
			timeSeries.add(new FixedMillisecond(entry.getKey()), entry.getValue());
		}
		lineDataset.addSeries(timeSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(" ", "millisecond", "Number of connections", lineDataset, true, true, true);
		//设置子标题
		TextTitle subtitle = new TextTitle("统计", new Font("黑体", Font.BOLD, 24));
		chart.addSubtitle(subtitle);
		//设置主标题
		chart.setTitle(new TextTitle("链接量统计", new Font("隶书", Font.ITALIC, 30)));
		chart.setAntiAlias(true);
		String filename=null;
		try {
			filename = ServletUtilities.saveChartAsPNG(chart, 1000, 600, null,request.getSession());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String graphURL = request.getContextPath() + "/DisplayChart?filename=" + filename;
        ModelAndView mav = new ModelAndView();
        mav.addObject("graphURL", graphURL);
        mav.addObject("filename", filename);
        mav.setViewName("session/chart");
        return mav;
	}
}
