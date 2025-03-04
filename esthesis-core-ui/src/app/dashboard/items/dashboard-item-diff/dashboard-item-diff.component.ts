import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {DashboardItemComponent} from "../dashboard-item.component";
import {BaseChartDirective} from "ng2-charts";
import {Subscription} from "rxjs";
import {DashboardUpdateDiffDto} from "../../dto/updates/DashboardUpdateDiffDto";
import * as d3 from "d3";
import {DashboardItemDiffConfigurationDto} from "../../dto/configuration/dashboard-item-diff-configuration-dto";

interface DataPoint {
  date: Date;
  value0: number;
  value1: number;
}

@Component({
  selector: 'app-dashboard-item-diff',
  templateUrl: './dashboard-item-diff.component.html'
})
export class DashboardItemDiffComponent extends DashboardItemComponent<DashboardUpdateDiffDto, DashboardItemDiffConfigurationDto>
  implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<"line"> | undefined;
  // A subscription to receive notification from the superclass when lastMessage is updated.
  lastMessageSubscription?: Subscription;
  data: DataPoint[] = [];

  override ngOnInit(): void {
    super.ngOnInit();
    this.lastMessageSubscription = this.lastMessageEmitter.subscribe(lastMessage => {
      const newValue: number =  Number(lastMessage.value);
      if (this.data.length > 0) {
        this.data.push({date: new Date(), value0: this.data[this.data.length - 1].value1, value1: newValue});
      } else {
        this.data.push({date: new Date(), value0: 0.0, value1: newValue});
      }
      // Trim to maximum number of items set.
      if (this.data.length > this.config?.items!) {
        this.data.shift();
      }

      document.getElementById('chart-container')?.children.item(0)?.remove();
      this.createChart();
    });
    this.createChart();
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    if (this.lastMessageSubscription) {
      this.lastMessageSubscription.unsubscribe();
    }
  }

  private createChart(): void {
    const width = document.getElementById('chart-container')!.offsetWidth;
    const height = 200;
    const marginTop = 20;
    const marginRight = 20;
    const marginBottom = 30;
    const marginLeft = 30;

    interface DataPoint {
      date: Date;
      value0: number;
      value1: number;
    }

    // Create the positional and color scales.
    const x = d3.scaleTime()
      .domain(d3.extent(this.data, d => d.date) as [Date, Date])
      .range([marginLeft, width - marginRight]);

    const y = d3.scaleLinear()
      .domain([
        d3.min(this.data, d => Math.min(d.value0, d.value1)) as number,
        d3.max(this.data, d => Math.max(d.value0, d.value1)) as number
      ])
      .range([height - marginBottom, marginTop]);

    const colors = [d3.schemeRdYlBu[3][2], d3.schemeRdYlBu[3][0]];

    // Create the SVG container.
    const svg = d3.create("svg")
      .attr("viewBox", `0 0 ${width} ${height}`)
      .attr("style", "max-width: 100%; height: auto; font: 10px sans-serif;")
      .datum(this.data);

    // Create the axes.
    svg.append("g")
      .attr("transform", `translate(0,${height - marginBottom})`)
      .call(d3.axisBottom(x)
        .ticks(width / 80)
        .tickSizeOuter(0))
      .call(g => g.select(".domain").remove());

    svg.append("g")
      .attr("transform", `translate(${marginLeft},0)`)
      .call(d3.axisLeft(y))
      .call(g => g.select(".domain").remove())
      .call(g => g.selectAll(".tick line").clone()
        .attr("x2", width - marginLeft - marginRight)
        .attr("stroke-opacity", 0.1))
      .call(g => g.select(".tick:last-of-type text").clone()
        .attr("x", -marginLeft)
        .attr("y", -30)
        .attr("fill", "currentColor")
        .attr("text-anchor", "start"));

    // Create the clipPaths.
    svg.append("clipPath")
      .attr("id", "above")
      .append("path")
      .attr("d", d3.area<DataPoint>()
        .curve(d3.curveStep)
        .x(d => x(d.date)!)
        .y0(0)
        .y1(d => y(d.value1)!));

    svg.append("clipPath")
      .attr("id", "below")
      .append("path")
      .attr("d", d3.area<DataPoint>()
        .curve(d3.curveStep)
        .x(d => x(d.date)!)
        .y0(height)
        .y1(d => y(d.value1)!));

    // Create the color areas.
    svg.append("path")
      .attr("clip-path", "url(#above)")
      .attr("fill", colors[1])
      .attr("d", d3.area<DataPoint>()
        .curve(d3.curveStep)
        .x(d => x(d.date)!)
        .y0(height)
        .y1(d => y(d.value0)!));

    svg.append("path")
      .attr("clip-path", "url(#below)")
      .attr("fill", colors[0])
      .attr("d", d3.area<DataPoint>()
        .curve(d3.curveStep)
        .x(d => x(d.date)!)
        .y0(0)
        .y1(d => y(d.value0)!));

    // Create the black line.
    svg.append("path")
      .attr("fill", "none")
      .attr("stroke", "black")
      .attr("stroke-width", 1.5)
      .attr("stroke-linejoin", "round")
      .attr("stroke-linecap", "round")
      .attr("d", d3.line<DataPoint>()
        .curve(d3.curveStep)
        .x(d => x(d.date)!)
        .y(d => y(d.value0)!));

    // Append svg to html.
    document.getElementById('chart-container')?.appendChild(svg.node()!);
  }

}
