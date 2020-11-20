import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard-charts',
  templateUrl: './dashboard-charts.component.html',
  styleUrls: ['./dashboard-charts.component.scss']
})
export class DashboardChartsComponent implements OnInit {
  areaChartEnergy = [
    {
      "name": "Watts",
      "series": [

        {
          "name": 1,
          "value": 28937
        },
        {
          "name": 2,
          "value": 22881
        },
        {
          "name": 3,
          "value": 36207
        },
        {
          "name": 4,
          "value": 23000
        },
        {
          "name": 5,
          "value": 28504
        },
        {
          "name": 6,
          "value": 17272
        },
        {
          "name": 7,
          "value": 26649
        },
        {
          "name": 8,
          "value": 16531
        },
        {
          "name": 9,
          "value": 21051
        },
        {
          "name": 10,
          "value": 22509
        },
        {
          "name": 11,
          "value": 19594
        },
        {
          "name": 12,
          "value": 20243
        },
        {
          "name": 13,
          "value": 30667
        },
        {
          "name": 14,
          "value": 36227
        },
        {
          "name": 15,
          "value": 20396
        },
        {
          "name": 16,
          "value": 39493
        },
        {
          "name": 17,
          "value": 34556
        },
        {
          "name": 18,
          "value": 39739
        },
        {
          "name": 19,
          "value": 26430
        },
        {
          "name": 20,
          "value": 22470
        },
        {
          "name": 21,
          "value": 38255
        },
        {
          "name": 22,
          "value": 23249
        },
        {
          "name": 23,
          "value": 19013
        },
        {
          "name": 24,
          "value": 24809
        },
        {
          "name": 25,
          "value": 18231
        },
        {
          "name": 26,
          "value": 37415
        },
        {
          "name": 27,
          "value": 31009
        },
        {
          "name": 28,
          "value": 26492
        },
        {
          "name": 29,
          "value": 29973
        },
        {
          "name": 30,
          "value": 28571
        },
        {
          "name": 31,
          "value": 26448
        },
        {
          "name": 32,
          "value": 24562
        },
        {
          "name": 33,
          "value": 31368
        },
        {
          "name": 34,
          "value": 38262
        },
        {
          "name": 35,
          "value": 31825
        },
        {
          "name": 36,
          "value": 31649
        },
        {
          "name": 37,
          "value": 28406
        },
        {
          "name": 38,
          "value": 36782
        },
        {
          "name": 39,
          "value": 16205
        },
        {
          "name": 40,
          "value": 22170
        },
        {
          "name": 41,
          "value": 35522
        },
        {
          "name": 42,
          "value": 21963
        },
        {
          "name": 43,
          "value": 32666
        },
        {
          "name": 44,
          "value": 29325
        },
        {
          "name": 45,
          "value": 23091
        },
        {
          "name": 46,
          "value": 22536
        },
        {
          "name": 47,
          "value": 34318
        },
        {
          "name": 48,
          "value": 21118
        },
        {
          "name": 49,
          "value": 18540
        },
        {
          "name": 50,
          "value": 35041
        },
        {
          "name": 51,
          "value": 16777
        },
        {
          "name": 52,
          "value": 19784
        },
        {
          "name": 53,
          "value": 38042
        },
        {
          "name": 54,
          "value": 26831
        },
        {
          "name": 55,
          "value": 36112
        },
        {
          "name": 56,
          "value": 21275
        },
        {
          "name": 57,
          "value": 39298
        },
        {
          "name": 58,
          "value": 37528
        },
        {
          "name": 59,
          "value": 15672
        },
        {
          "name": 60,
          "value": 25458
        },
        {
          "name": 61,
          "value": 16431
        },
        {
          "name": 62,
          "value": 36839
        },
        {
          "name": 63,
          "value": 24980
        },
        {
          "name": 64,
          "value": 39460
        },
        {
          "name": 65,
          "value": 24423
        },
        {
          "name": 66,
          "value": 32297
        },
        {
          "name": 67,
          "value": 24678
        },
        {
          "name": 68,
          "value": 34738
        },
        {
          "name": 69,
          "value": 33566
        },
        {
          "name": 70,
          "value": 15921
        },
        {
          "name": 71,
          "value": 21721
        },
        {
          "name": 72,
          "value": 30592
        },
        {
          "name": 73,
          "value": 17251
        },
        {
          "name": 74,
          "value": 28386
        },
        {
          "name": 75,
          "value": 29801
        },
        {
          "name": 76,
          "value": 15299
        },
        {
          "name": 77,
          "value": 34507
        },
        {
          "name": 78,
          "value": 38650
        },
        {
          "name": 79,
          "value": 38506
        },
        {
          "name": 80,
          "value": 32019
        },
        {
          "name": 81,
          "value": 36820
        },
        {
          "name": 82,
          "value": 32245
        },
        {
          "name": 83,
          "value": 36453
        },
        {
          "name": 84,
          "value": 20130
        },
        {
          "name": 85,
          "value": 31455
        },
        {
          "name": 86,
          "value": 35336
        },
        {
          "name": 87,
          "value": 18340
        },
        {
          "name": 88,
          "value": 28815
        },
        {
          "name": 89,
          "value": 25992
        },
        {
          "name": 90,
          "value": 27839
        },
        {
          "name": 91,
          "value": 18700
        },
        {
          "name": 92,
          "value": 33852
        },
        {
          "name": 93,
          "value": 33349
        },
        {
          "name": 94,
          "value": 33315
        },
        {
          "name": 95,
          "value": 17366
        },
        {
          "name": 96,
          "value": 35761
        },
        {
          "name": 97,
          "value": 34096
        },
        {
          "name": 98,
          "value": 38904
        },
        {
          "name": 99,
          "value": 35710
        },
        {
          "name": 100,
          "value": 35623
        }

      ]
    },
  ];
  areaChartOil = (JSON.parse(JSON.stringify(this.areaChartEnergy)));
  areaChartGas = (JSON.parse(JSON.stringify(this.areaChartEnergy)));
  areaChartWater = (JSON.parse(JSON.stringify(this.areaChartEnergy)));

  constructor() { }

  ngOnInit() {
    this.areaChartEnergy[0]['name'] = 'kWh';
    this.areaChartOil[0]['name'] = 'lt';
    this.areaChartGas[0]['name'] = 'kWh';
    this.areaChartWater[0]['name'] = 'lt';
  }

}
