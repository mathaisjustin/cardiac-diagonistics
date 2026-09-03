const StatisticsSection = () => {
  const statistics = [
    {
      value: '1,024',
      label: 'Records',
    },
    {
      value: '54.1',
      label: 'Mean age',
    },
    {
      value: '41%',
      label: 'Surgery share',
    },
    {
      value: 'LIVE',
      label: 'Sourced per request',
    },
  ]

  return (
    <section className="grid grid-cols-4 border-b border-gray-300 bg-[#f3f1f1]">
      {statistics.map((statistic) => (
        <div
          key={statistic.label}
          className="border-r border-gray-300 px-8 py-7 last:border-r-0"
        >
          <p
            className={`text-4xl font-extrabold ${
              statistic.value === 'LIVE'
                ? 'text-[#ed3217]'
                : 'text-gray-900'
            }`}
          >
            {statistic.value}
          </p>

          <p className="mt-2 text-xs font-bold uppercase tracking-[0.12em] text-gray-500">
            {statistic.label}
          </p>
        </div>
      ))}
    </section>
  )
}

export default StatisticsSection